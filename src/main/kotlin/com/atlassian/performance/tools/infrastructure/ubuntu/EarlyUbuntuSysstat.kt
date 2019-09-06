package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.TcpServerHook
import com.atlassian.performance.tools.ssh.api.SshConnection

class EarlyUbuntuSysstat : TcpServerHook {

    override fun run(
        ssh: SshConnection,
        server: TcpServer,
        flow: JiraNodeFlow
    ) {
        UbuntuSysstat()
            .install(ssh)
            .forEach { flow.hookPreInstall(it) }
    }
}
