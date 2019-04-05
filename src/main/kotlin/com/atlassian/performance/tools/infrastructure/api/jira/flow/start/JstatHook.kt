package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JstatHook : PostStartHook {

    override fun hook(
        ssh: SshConnection,
        jira: StartedJira,
        track: ReportTrack
    ) {
        val process = jira.installed.jdk.jstatMonitoring.start(ssh, jira.pid)
        track.reports.add(RemoteMonitoringProcessReport(process))
    }
}
