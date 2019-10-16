package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.api.jira.hook.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.PreInstallHook
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.jira.hook.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class InstalledOsMetric(
    private val metric: OsMetric
) : PreInstallHook {

    override fun run(ssh: SshConnection, server: TcpServer, hooks: PreInstallHooks) {
        val process = metric.start(ssh)
        hooks.addReport(RemoteMonitoringProcessReport(process))
    }
}
