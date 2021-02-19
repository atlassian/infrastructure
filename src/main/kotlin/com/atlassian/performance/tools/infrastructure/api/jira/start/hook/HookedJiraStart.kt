package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraStart
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira

class HookedJiraStart(
    private val start: JiraStart,
    private val hooks: PreStartHooks
) : JiraStart {

    override fun start(
        installed: InstalledJira
    ): StartedJira {
        installed.server.ssh.newConnection().use { ssh ->
            hooks.call(ssh, installed)
        }
        val started = start.start(installed)
        installed.server.ssh.newConnection().use { ssh ->
            hooks.postStart.call(ssh, started)
        }
        return started
    }
}
