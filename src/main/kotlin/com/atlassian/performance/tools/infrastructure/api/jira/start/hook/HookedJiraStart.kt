package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraStart
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class HookedJiraStart(
    private val start: JiraStart,
    private val hooks: PreStartHooks
) : JiraStart {

    override fun start(
        ssh: SshConnection,
        installed: InstalledJira
    ): StartedJira {
        hooks.call(ssh, installed)
        val started = start.start(ssh, installed)
        hooks.postStart.call(ssh, started)
        return started
    }
}
