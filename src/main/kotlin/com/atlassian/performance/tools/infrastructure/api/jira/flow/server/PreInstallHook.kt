package com.atlassian.performance.tools.infrastructure.api.jira.flow.server

import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.PreInstallFlow
import com.atlassian.performance.tools.ssh.api.SshConnection

interface PreInstallHook {
    fun run(
        ssh: SshConnection,
        server: TcpServer,
        flow: PreInstallFlow
    )
}
