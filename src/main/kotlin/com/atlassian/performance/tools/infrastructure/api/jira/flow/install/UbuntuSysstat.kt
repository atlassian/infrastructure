package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportSequence
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.PassingUpgrade
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class UbuntuSysstat : Install {
    override fun install(
        ssh: SshConnection,
        jira: InstalledJira
    ): Start = Ubuntu()
        .metrics(ssh)
        .let { OsMetricStart(it) }
}

private class OsMetricStart(
    private val metrics: List<OsMetric>
) : Start {

    override fun start(
        ssh: SshConnection,
        jira: InstalledJira
    ): Upgrade = metrics
        .map { it.start(ssh) }
        .map { RemoteMonitoringProcessReport(it) }
        .let { ReportSequence(it) }
        .let { PassingUpgrade(it) }
}
