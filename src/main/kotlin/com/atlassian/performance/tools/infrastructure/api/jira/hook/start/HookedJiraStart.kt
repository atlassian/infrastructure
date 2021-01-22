package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.StartedJira
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
