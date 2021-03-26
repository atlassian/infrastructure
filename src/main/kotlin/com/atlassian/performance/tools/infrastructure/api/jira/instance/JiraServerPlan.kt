package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import java.net.URI

class JiraServerPlan private constructor(
    private val plan: JiraNodePlan,
    private val infrastructure: Infrastructure,
    private val hooks: PreInstanceHooks
) : JiraInstancePlan {

    private val reports: Reports = Reports()

    override fun materialize(): JiraInstance {
        val nodeHooks = listOf(plan).map { it.hooks }
        hooks.call(nodeHooks, reports)
        val jiraNode = infrastructure.serve(8080, "jira-node")
        val installed = plan.installation.install(jiraNode, reports)
        val started = plan.start.start(installed, reports)
        val instance = JiraServer(started)
        hooks.postInstance.call(instance, reports)
        return instance
    }

    override fun report(): Reports = reports.copy()

    private class JiraServer(
        node: StartedJira
    ) : JiraInstance {
        override val address: URI = node.installed.host.run { URI("http://$ip:$publicPort/") }
        override val nodes: List<StartedJira> = listOf(node)
    }

    class Builder(
        private var infrastructure: Infrastructure
    ) {
        private var plan: JiraNodePlan = JiraNodePlan.Builder().build()
        private var hooks: PreInstanceHooks = PreInstanceHooks.default()

        fun plan(plan: JiraNodePlan) = apply { this.plan = plan }
        fun infrastructure(infrastructure: Infrastructure) = apply { this.infrastructure = infrastructure }
        fun hooks(hooks: PreInstanceHooks) = apply { this.hooks = hooks }

        fun build(): JiraInstancePlan = JiraServerPlan(plan, infrastructure, hooks)
    }
}