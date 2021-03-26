package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNode
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.loadbalancer.LoadBalancer
import java.net.URI
import java.time.Duration
import java.util.function.Supplier
import kotlin.streams.asStream
import kotlin.streams.toList

class JiraDataCenterPlan constructor(
    val nodePlans: List<JiraNodePlan>,
    val instanceHooks: PreInstanceHooks,
    val loadBalancerSupplier: Supplier<LoadBalancer>,
    val infrastructure: Infrastructure
) : JiraInstancePlan {

    private val reports: Reports = Reports()
    private val loadBalancingPatience = Duration.ofMinutes(5)

    override fun materialize(): JiraInstance {
        instanceHooks.call(nodePlans.map { it.hooks }, reports)
        val nodes = infrastructure.serve(nodePlans)
        val installed = installInParallel(nodes)
        val started = installed.map { it.start(reports) }
        val loadBalancer = loadBalancerSupplier.get()
        val instance = JiraDataCenter(started, loadBalancer)
        instanceHooks.postInstance.call(instance, reports)
        loadBalancer.waitUntilHealthy(loadBalancingPatience)
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
        private var nodePlans: List<JiraNodePlan> = listOf(1, 2).map { JiraNodePlan.Builder().build() }
//  TODO      private var instanceHooks: PreInstanceHooks =
//            PreInstanceHooks(listOf(1..2).map { PreInstallHooks.default() }) // TODO two lists in sync? gotta be a better way
//
//        fun build(): Supplier<JiraInstance> = JiraDataCenterPlan(nodePlans, infrastructure)
    }
}