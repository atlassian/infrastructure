package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.Iostat
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.api.os.Vmstat
import com.atlassian.performance.tools.infrastructure.jira.report.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class LateUbuntuSysstat : PostInstallHook {
    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks,
        reports: Reports
    ) {
        val ubuntu = Ubuntu()
        ubuntu.install(ssh, listOf("sysstat"))
        listOf(Vmstat(), Iostat())
            .map { PostStartOsMetric(it) }
            .forEach { hooks.postStart.insert(it) }
    }
}

private class PostStartOsMetric(
    private val metric: OsMetric
) : PostStartHook {
    override fun call(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks, reports: Reports) {
        val process = metric.start(ssh)
        reports.add(RemoteMonitoringProcessReport(process), jira)
    }
}
