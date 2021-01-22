package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PreInstallHook
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.jira.hook.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class InstalledOsMetric(
    private val metric: OsMetric
) : PreInstallHook {

    override fun call(ssh: SshConnection, server: TcpServer, hooks: PreInstallHooks) {
        val process = metric.start(ssh)
        hooks.reports.add(RemoteMonitoringProcessReport(process))
    }
}
