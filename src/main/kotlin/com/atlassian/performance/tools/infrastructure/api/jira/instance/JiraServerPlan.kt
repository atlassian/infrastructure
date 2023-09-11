package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.network.HttpServerRoom

class JiraServerPlan private constructor(
    private val plan: JiraNodePlan,
    private val hooks: PreInstanceHooks
) : JiraInstancePlan {

    private val reports: Reports = Reports()

    override fun materialize(): JiraInstance {
        val nodeHooks = listOf(plan).map { it.hooks }
        hooks.call(nodeHooks, reports)
        val http = plan.serverRoom.serveHttp("jira-node")
        val installed = plan.installation.install(http, reports)
        val started = plan.start.start(installed, reports)
        val instance = JiraServer(started)
        hooks.postInstance.call(instance, reports)
        return instance
    }

    override fun report(): Reports = reports.copy()

    private class JiraServer(
        node: StartedJira
    ) : JiraInstance {
        override val address: HttpNode = node.installed.http
        override val nodes: List<StartedJira> = listOf(node)
    }

    class Builder(
        private var plan: JiraNodePlan

    ) {
        private var hooks: PreInstanceHooks = PreInstanceHooks.default()

        constructor(serverRoom: HttpServerRoom) : this(
            plan = JiraNodePlan.Builder(serverRoom).build()
        )

        fun plan(plan: JiraNodePlan) = apply { this.plan = plan }
        fun hooks(hooks: PreInstanceHooks) = apply { this.hooks = hooks }

        fun build(): JiraInstancePlan = JiraServerPlan(plan, hooks)
    }
}
