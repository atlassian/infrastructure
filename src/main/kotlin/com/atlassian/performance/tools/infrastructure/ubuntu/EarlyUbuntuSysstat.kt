package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.PreInstallFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.PreInstallHook
import com.atlassian.performance.tools.ssh.api.SshConnection

class EarlyUbuntuSysstat : PreInstallHook {

    override fun run(
        ssh: SshConnection,
        server: TcpServer,
        flow: PreInstallFlow
    ) {
        UbuntuSysstat()
            .install(ssh)
            .forEach { flow.hook(it) }
    }
}
