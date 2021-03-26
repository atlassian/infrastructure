package com.atlassian.performance.tools.infrastructure.api.jira.node

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraStart
import com.atlassian.performance.tools.infrastructure.api.jvm.OracleJDK
import com.atlassian.performance.tools.infrastructure.jira.install.hook.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.jira.start.hook.HookedJiraStart
import net.jcip.annotations.NotThreadSafe

class JiraNodePlan private constructor(
    val installation: JiraInstallation,
    val start: JiraStart,
    internal val hooks: PreInstallHooks
) {

    fun materialize(host: TcpHost) = JiraNode(host, this)

    @NotThreadSafe
    class Builder {
        private var installation: JiraInstallation = ParallelInstallation(
            EmptyJiraHome(),
            PublicJiraSoftwareDistribution("7.13.0"),
            OracleJDK()
        )
        private var start: JiraStart = JiraLaunchScript()
        private var hooks: PreInstallHooks = PreInstallHooks.default()

        fun installation(installation: JiraInstallation) = apply { this.installation = installation }
        fun start(start: JiraStart) = apply { this.start = start }
        fun hooks(hooks: PreInstallHooks) = apply { this.hooks = hooks }

        fun build() = JiraNodePlan(
            HookedJiraInstallation(installation, hooks),
            HookedJiraStart(start, hooks.preStart),
            hooks
        )
    }
}
