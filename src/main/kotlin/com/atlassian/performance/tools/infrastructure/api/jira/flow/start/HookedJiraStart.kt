package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.ssh.api.SshConnection

class HookedJiraStart(
    private val start: JiraStart
) : JiraStart {

    override fun start(
        ssh: SshConnection,
        installed: InstalledJira,
        track: ReportTrack
    ): StartedJira {
        track.preStartHooks.forEach { it.hook(ssh, installed, track) }
        val started = start.start(ssh, installed, track)
        track.postStartHooks.forEach { it.hook(ssh, started, track) }
        return started
    }
}
