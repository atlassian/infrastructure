package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.PreInstallFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.PreInstallHook
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class InstalledOsMetric(
    private val metric: OsMetric
) : PreInstallHook {

    override fun run(ssh: SshConnection, server: TcpServer, flow: PreInstallFlow) {
        val process = metric.start(ssh)
        flow.addReport(RemoteMonitoringProcessReport(process))
    }
}
