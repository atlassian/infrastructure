package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import java.net.URI

class JiraServerPlan private constructor(
    private val plan: JiraNodePlan,
    private val hooks: PreInstanceHooks
) : JiraInstancePlan {

    private val reports: Reports = Reports()

    override fun materialize(): JiraInstance {
        val nodeHooks = listOf(plan).map { it.hooks }
        hooks.call(nodeHooks, reports)
        val http = plan.infrastructure.serveHttp("jira-node")
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
        override val address: URI = node.installed.http.addressPublicly()
        override val nodes: List<StartedJira> = listOf(node)
    }

    class Builder(
        infrastructure: Infrastructure
    ) {
        private var plan: JiraNodePlan = JiraNodePlan.Builder(infrastructure).build()
        private var hooks: PreInstanceHooks = PreInstanceHooks.default()

        fun plan(plan: JiraNodePlan) = apply { this.plan = plan }
        fun hooks(hooks: PreInstanceHooks) = apply { this.hooks = hooks }

        fun build(): JiraInstancePlan = JiraServerPlan(plan, hooks)
    }
}