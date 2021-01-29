package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHook
import com.atlassian.performance.tools.ssh.api.SshConnection

class EarlyUbuntuSysstat : PreInstallHook {

    override fun call(
        ssh: SshConnection,
        server: TcpServer,
        hooks: PreInstallHooks
    ) {
        UbuntuSysstat()
            .install(ssh)
            .forEach { hooks.insert(it) }
    }
}
