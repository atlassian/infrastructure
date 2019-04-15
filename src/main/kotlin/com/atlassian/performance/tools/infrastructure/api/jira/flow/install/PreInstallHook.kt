package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.ssh.api.SshConnection

interface PreInstallHook {
    fun hook(
        ssh: SshConnection,
        server: TcpServer,
        flow: JiraNodeFlow
    )
}
