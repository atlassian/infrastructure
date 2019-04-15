package com.atlassian.performance.tools.infrastructure.api.jira.flow.server

import com.atlassian.performance.tools.infrastructure.Iostat
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.api.os.Vmstat
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class UbuntuSysstat : TcpServerHook {

    override fun hook(
        ssh: SshConnection,
        server: TcpServer,
        flow: JiraNodeFlow
    ) {
        val ubuntu = Ubuntu()
        ubuntu.install(ssh, listOf("sysstat"))
        listOf(Vmstat(), Iostat())
            .map { InstalledOsMetric(it) }
            .forEach { flow.hookPostStart(it) }
    }
}

private class InstalledOsMetric(
    private val metric: OsMetric
) : TcpServerHook {

    override fun hook(ssh: SshConnection, server: TcpServer, flow: JiraNodeFlow) {
        val process = metric.start(ssh)
        flow.reports.add(RemoteMonitoringProcessReport(process))
    }
}
