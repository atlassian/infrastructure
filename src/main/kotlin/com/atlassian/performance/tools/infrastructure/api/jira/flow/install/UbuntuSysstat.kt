package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class UbuntuSysstat : PostInstallHook {

    override fun hook(
        ssh: SshConnection,
        jira: InstalledJira,
        track: ReportTrack
    ) {
        val postStartHooks = Ubuntu()
            .metrics(ssh)
            .map { InstalledOsMetric(it) }
        track.postStartHooks.addAll(postStartHooks)
    }
}

private class InstalledOsMetric(
    private val metric: OsMetric
) : PostStartHook {

    override fun hook(
        ssh: SshConnection,
        jira: StartedJira,
        track: ReportTrack
    ) {
        val process = metric.start(ssh)
        track.reports.add(RemoteMonitoringProcessReport(process))
    }
}
