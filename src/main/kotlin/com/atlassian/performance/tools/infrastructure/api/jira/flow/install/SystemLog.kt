package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class SystemLog : PreInstallHook {
    override fun hook(ssh: SshConnection, server: TcpServer, flow: JiraNodeFlow) {
        flow.reports.add(StaticReport("/var/log/syslog"))
    }
}
