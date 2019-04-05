package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JiraLogs : PreStartHook, PostStartHook {

    override fun hook(ssh: SshConnection, jira: InstalledJira, track: ReportTrack) {
        listOf(
            StaticReport("${jira.home}/log/atlassian-jira.log"),
            StaticReport("${jira.installation}/logs/catalina.out")
        ).forEach { track.reports.add(it) }
    }

    override fun hook(ssh: SshConnection, jira: StartedJira, track: ReportTrack) {
        hook(ssh, jira.installed, track)
    }
}
