package com.atlassian.performance.tools.infrastructure.hookapi.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.hookapi.loadbalancer.ApacheProxyPlan
import com.atlassian.performance.tools.infrastructure.hookapi.loadbalancer.LoadBalancerPlan
import com.atlassian.performance.tools.infrastructure.hookapi.network.HttpServerRoom
import java.time.Duration
import kotlin.streams.asStream
import kotlin.streams.toList

class JiraDataCenterPlan private constructor(
    private val nodePlans: List<JiraNodePlan>,
    private val instanceHooks: PreInstanceHooks,
    private val balancerPlan: LoadBalancerPlan
) : JiraInstancePlan {

    private val reports: Reports = Reports()
    private val loadBalancingPatience = Duration.ofMinutes(5)

    override fun materialize(): JiraInstance {
        instanceHooks.call(nodePlans.map { it.hooks }, reports)
        val http = nodePlans.mapIndexed { nodeIndex, plan ->
            val nodeNumber = nodeIndex + 1
            val http = plan.serverRoom.serveHttp("jira-node-$nodeNumber")
            PlannedHttpNode(http, plan)
        }
        val balancer = balancerPlan.materialize(http.map { it.http }, nodePlans.map { it.hooks.preStart })
        val installed = installInParallel(http)
        val started = installed.map { it.start(reports) }
        val instance = JiraDataCenter(started, balancer)
        instanceHooks.postInstance.call(instance, reports)
        return instance
    }

    override fun report(): Reports = reports.copy()

    private fun installInParallel(nodes: Collection<PlannedHttpNode>): List<PlannedInstalledJira> = nodes
        .asSequence()
        .asStream()
        .parallel()
        .map { it.install(reports) }
        .toList()

    private class PlannedHttpNode(
        val http: HttpNode,
        val plan: JiraNodePlan
    ) {
        fun install(reports: Reports): PlannedInstalledJira {
            return plan.installation.install(http, reports).let { PlannedInstalledJira(it, plan) }
        }
    }

    private class PlannedInstalledJira(
        val installed: InstalledJira,
        val plan: JiraNodePlan
    ) {
        fun start(reports: Reports): StartedJira {
            return plan.start.start(installed, reports)
        }
    }

    private class JiraDataCenter(
        override val nodes: List<StartedJira>,
        loadBalancer: HttpNode
    ) : JiraInstance {
        override val address = loadBalancer
    }

    class Builder(
        private var nodePlans: List<JiraNodePlan>,
        private var balancerPlan: LoadBalancerPlan
    ) {
        private var instanceHooks: PreInstanceHooks = PreInstanceHooks.default()

        constructor(
            serverRoom: HttpServerRoom
        ) : this(
            nodePlans = listOf(1, 2).map {
                JiraNodePlan.Builder(serverRoom)
                    .dataCenter()
                    .build()
            },
            balancerPlan = ApacheProxyPlan(serverRoom)
        )

        fun nodePlans(nodePlans: List<JiraNodePlan>) = apply { this.nodePlans = nodePlans }
        fun instanceHooks(instanceHooks: PreInstanceHooks) = apply { this.instanceHooks = instanceHooks }
        fun balancerPlan(balancerPlan: LoadBalancerPlan) = apply { this.balancerPlan = balancerPlan }

        fun build(): JiraInstancePlan = JiraDataCenterPlan(nodePlans, instanceHooks, balancerPlan)
    }
}
