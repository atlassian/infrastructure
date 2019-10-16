package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.api.jira.hook.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.PreInstallHook
import com.atlassian.performance.tools.ssh.api.SshConnection

class EarlyUbuntuSysstat : PreInstallHook {

    override fun run(
        ssh: SshConnection,
        server: TcpServer,
        hooks: PreInstallHooks
    ) {
        UbuntuSysstat()
            .install(ssh)
            .forEach { hooks.hook(it) }
    }
}
