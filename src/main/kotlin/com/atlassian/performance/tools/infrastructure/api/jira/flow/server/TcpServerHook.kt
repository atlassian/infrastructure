package com.atlassian.performance.tools.infrastructure.api.jira.flow.server

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHook
import com.atlassian.performance.tools.ssh.api.SshConnection

interface TcpServerHook : InstalledJiraHook {

    fun run(
        ssh: SshConnection,
        server: TcpServer,
        flow: JiraNodeFlow
    )

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: JiraNodeFlow
    ) {
        run(ssh, jira.server, flow)
    }
}
