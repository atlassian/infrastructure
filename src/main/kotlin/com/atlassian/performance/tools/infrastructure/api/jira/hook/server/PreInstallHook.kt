package com.atlassian.performance.tools.infrastructure.api.jira.hook.server

import com.atlassian.performance.tools.infrastructure.api.jira.hook.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.hook.PreInstallHooks
import com.atlassian.performance.tools.ssh.api.SshConnection

interface PreInstallHook {
    fun run(
        ssh: SshConnection,
        server: TcpServer,
        hooks: PreInstallHooks
    )
}
