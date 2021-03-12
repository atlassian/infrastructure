package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraStart
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.HookedJiraStart
import com.atlassian.performance.tools.infrastructure.api.jvm.OracleJDK
import java.net.URI
import java.util.function.Supplier

class JiraServerPlan private constructor(
    private val infrastructure: Infrastructure,
    val hooks: PreInstallHooks,
    val installation: JiraInstallation,
    val start: JiraStart
) : Supplier<JiraInstance> {

    override fun get(): JiraServer {
        val jiraNode = infrastructure.serve(8080, "jira-node")
        val installed = installation.install(jiraNode)
        val started = start.start(installed)
        return JiraServer(started)
    }

    class JiraServer(
        val node: StartedJira
    ) : JiraInstance {
        override val address: URI = node.installed.host.run { URI("http://$ip:$publicPort/") }
    }

    class Builder(
        private var infrastructure: Infrastructure
    ) {
        private var hooks: PreInstallHooks = PreInstallHooks.default()
        private var installation: JiraInstallation = HookedJiraInstallation(
            ParallelInstallation(
                EmptyJiraHome(),
                PublicJiraSoftwareDistribution("7.13.0"),
                OracleJDK()
            ),
            hooks
        )
        private var start: JiraStart = HookedJiraStart(JiraLaunchScript(), hooks.preStart)

        fun infrastructure(infrastructure: Infrastructure) = apply { this.infrastructure = infrastructure }
        fun hooks(hooks: PreInstallHooks) = apply { this.hooks = hooks } // TODO this doesn't affect the start or installation
        fun installation(installation: JiraInstallation) = apply { this.installation = installation }
        fun start(start: JiraStart) = apply { this.start = start }

        fun build() = JiraServerPlan(
            infrastructure = infrastructure,
            hooks = hooks,
            installation = installation,
            start = start
        )
    }
}