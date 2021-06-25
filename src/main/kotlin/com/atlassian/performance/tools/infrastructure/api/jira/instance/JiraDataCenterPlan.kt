package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNode
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.loadbalancer.ApacheProxyPlan
import com.atlassian.performance.tools.infrastructure.api.loadbalancer.LoadBalancer
import com.atlassian.performance.tools.infrastructure.api.loadbalancer.LoadBalancerPlan
import java.net.URI
import java.time.Duration
import kotlin.streams.asStream
import kotlin.streams.toList

class JiraDataCenterPlan private constructor(
    private val nodePlans: List<JiraNodePlan>,
    private val instanceHooks: PreInstanceHooks,
    private val balancerPlan: LoadBalancerPlan,
    private val infrastructure: Infrastructure
) : JiraInstancePlan {

    private val reports: Reports = Reports()
    private val loadBalancingPatience = Duration.ofMinutes(5)

    override fun materialize(): JiraInstance {
        instanceHooks.call(nodePlans.map { it.hooks }, reports)
        val nodes = nodePlans.mapIndexed { nodeIndex, nodePlan ->
            val nodeNumber = nodeIndex + 1
            val host = infrastructure.serveTcp("jira-node-$nodeNumber")
            nodePlan.materialize(host)
        }
        val balancer = balancerPlan.materialize(nodes)
        val installed = installInParallel(nodes)
        val started = installed.map { it.start(reports) }
        val instance = JiraDataCenter(started, balancer)
        instanceHooks.postInstance.call(instance, reports)
        balancer.waitUntilHealthy(loadBalancingPatience)
        return instance
    }

    override fun report(): Reports = reports.copy()

    private fun installInParallel(nodes: List<JiraNode>) = nodes
        .asSequence()
        .asStream()
        .parallel()
        .map { jiraNode ->
            jiraNode
                .plan
                .installation
                .install(jiraNode.host, reports)
                .let { installedJira -> InstalledJiraNode(installedJira, jiraNode.plan) }
        }
        .toList()

    private class InstalledJiraNode(
        private val installedJira: InstalledJira,
        private val nodePlan: JiraNodePlan
    ) {
        fun start(reports: Reports): StartedJira {
            return nodePlan.start.start(installedJira, reports)
        }
    }

    private class JiraDataCenter(
        override val nodes: List<StartedJira>,
        private val loadBalancer: LoadBalancer
    ) : JiraInstance {
        override val address: URI
            get() = loadBalancer.uri
    }

    class Builder(
        private var infrastructure: Infrastructure
    ) {
        private var nodePlans: List<JiraNodePlan> = listOf(1, 2).map {
            JiraNodePlan.Builder()
                .hooks(PreInstallHooks.default().apply { postInstall.insert(DefaultClusterProperties()) })
                .build()
        }
        private var instanceHooks: PreInstanceHooks = PreInstanceHooks.default()
        private var balancerPlan: LoadBalancerPlan = ApacheProxyPlan(infrastructure)

        fun infrastructure(infrastructure: Infrastructure) = apply { this.infrastructure = infrastructure }
        fun nodePlans(nodePlans: List<JiraNodePlan>) = apply { this.nodePlans = nodePlans }
        fun instanceHooks(instanceHooks: PreInstanceHooks) = apply { this.instanceHooks = instanceHooks }
        fun balancerPlan(balancerPlan: LoadBalancerPlan) = apply { this.balancerPlan = balancerPlan }

        fun build(): JiraInstancePlan = JiraDataCenterPlan(nodePlans, instanceHooks, balancerPlan, infrastructure)
    }
}