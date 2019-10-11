package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.TcpServerHook
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class InstalledOsMetric(
    private val metric: OsMetric
) : TcpServerHook {

    override fun run(ssh: SshConnection, server: TcpServer, flow: JiraNodeFlow) {
        val process = metric.start(ssh)
        flow.reports.add(RemoteMonitoringProcessReport(process))
    }
}
