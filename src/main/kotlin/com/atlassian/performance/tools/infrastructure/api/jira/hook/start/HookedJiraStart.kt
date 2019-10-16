package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.JiraNodeHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class HookedJiraStart(
    private val start: JiraStart
) : JiraStart {

    override fun start(
        ssh: SshConnection,
        installed: InstalledJira,
        hooks: JiraNodeHooks
    ): StartedJira {
        hooks.runPreStartHooks(ssh, installed)
        val started = start.start(ssh, installed, hooks)
        hooks.runPostStartHooks(ssh, started)
        return started
    }
}
