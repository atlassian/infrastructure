package com.atlassian.performance.tools.infrastructure.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraStart
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PreStartHooks

class HookedJiraStart(
    private val start: JiraStart,
    private val hooks: PreStartHooks
) : JiraStart {

    override fun start(
        installed: InstalledJira,
        reports: Reports
    ): StartedJira {
        installed.http.tcp.ssh.newConnection().use { ssh ->
            hooks.call(ssh, installed, reports)
        }
        val started = start.start(installed, reports)
        installed.http.tcp.ssh.newConnection().use { ssh ->
            hooks.postStart.call(ssh, started, reports)
        }
        return started
    }
}
