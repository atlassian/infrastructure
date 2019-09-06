package com.atlassian.performance.tools.infrastructure.api.jira.flow.server

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.ubuntu.UbuntuSysstat
import com.atlassian.performance.tools.ssh.api.SshConnection

class LateUbuntuSysstat : TcpServerHook {

    override fun run(
        ssh: SshConnection,
        server: TcpServer,
        flow: JiraNodeFlow
    ) {
        UbuntuSysstat()
            .install(ssh)
            .forEach { flow.hookPostStart(it) }
    }
}
