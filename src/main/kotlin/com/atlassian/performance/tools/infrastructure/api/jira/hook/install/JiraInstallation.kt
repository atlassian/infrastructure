package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.ssh.api.SshConnection
import net.jcip.annotations.ThreadSafe

@ThreadSafe
interface JiraInstallation {

    fun install(
        ssh: SshConnection,
        server: TcpServer
    ): InstalledJira
}
