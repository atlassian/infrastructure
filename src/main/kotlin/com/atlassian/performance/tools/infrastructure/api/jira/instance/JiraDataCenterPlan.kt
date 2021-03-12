package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.loadbalancer.LoadBalancer
import java.net.URI
import java.time.Duration
import java.util.function.Supplier

class JiraDataCenterPlan constructor(
    val nodePlans: List<JiraNodePlan>,
    val instanceHooks: PreInstanceHooks,
    val loadBalancerSupplier: Supplier<LoadBalancer>,
    val infrastructure: Infrastructure
) : Supplier<JiraInstance> {
    
    private val loadBalancingPatience = Duration.ofMinutes(5)

    override fun get(): JiraDataCenter {
        instanceHooks.call()
        val started = infrastructure.serve(nodePlans).map { jiraNode ->
            jiraNode
                .plan
                .installation
                .install(jiraNode.host)
                .let { jiraNode.plan.start.start(it) }
        }
        val loadBalancer = loadBalancerSupplier.get()
        instanceHooks.postInstance.call(loadBalancer.uri)
        loadBalancer.waitUntilHealthy(loadBalancingPatience)
        return JiraDataCenter(started, loadBalancer)
    }

    class JiraDataCenter(
        val nodes: List<StartedJira>,
        val loadBalancer: LoadBalancer
    ) : JiraInstance {
        override val address: URI
            get() = loadBalancer.uri
    }
//
//    class Builder(
//        private var jiraNode: TcpServer
//    ) {
//        private var hooks: PreInstallHooks = PreInstallHooks.default()
//        private var installation: JiraInstallation = HookedJiraInstallation(
//            ParallelInstallation(
//                EmptyJiraHome(),
//                PublicJiraSoftwareDistribution("7.13.0"),
//                OracleJDK()
//            ),
//            hooks
//        )
//        private var start: JiraStart = HookedJiraStart(JiraLaunchScript(), hooks.preStart)
//
//        fun jiraNode(jiraNode: TcpServer) = apply { this.jiraNode = jiraNode }
//        fun hooks(hooks: PreInstallHooks) = apply { this.hooks = hooks } // TODO this doesn't affect the start or installation
//        fun installation(installation: JiraInstallation) = apply { this.installation = installation }
//        fun start(start: JiraStart) = apply { this.start = start }
//
//        fun build() = JiraDataCenterPlan(
//            jiraNode = jiraNode,
//            hooks = hooks,
//            installation = installation,
//            start = start
//        )
//    }
}