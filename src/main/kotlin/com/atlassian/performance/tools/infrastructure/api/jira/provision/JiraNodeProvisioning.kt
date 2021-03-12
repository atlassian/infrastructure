package com.atlassian.performance.tools.infrastructure.api.jira.provision

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jira.hook.JiraNodeHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.HookedJiraStart
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.JiraStart
import com.atlassian.performance.tools.infrastructure.api.jira.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraStart
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.HookedJiraStart
import com.atlassian.performance.tools.infrastructure.api.jvm.OracleJDK
import net.jcip.annotations.NotThreadSafe

class JiraNodeProvisioning private constructor(
    val hooks: PreInstallHooks,
    val installation: JiraInstallation,
    val start: JiraStart
) {

    @NotThreadSafe
    class Builder {
        private var hooks: PreInstallHooks = PreInstallHooks.default()
        private var installation: JiraInstallation = HookedJiraInstallation(
            ParallelInstallation(
                EmptyJiraHome(),
                PublicJiraSoftwareDistribution("7.13.0"),
                OracleJDK()
            ),
            hooks
        )
        private var start: JiraStart = HookedJiraStart(JiraLaunchScript())

        fun hooks(hooks: PreInstallHooks) = apply { this.hooks = hooks }
        fun installation(installation: JiraInstallation) = apply { this.installation = installation }
        fun start(start: JiraStart) = apply { this.start = start }

        fun build() = JiraNodeProvisioning(
            hooks = hooks,
            installation = installation,
            start = start
        )
    }
}
