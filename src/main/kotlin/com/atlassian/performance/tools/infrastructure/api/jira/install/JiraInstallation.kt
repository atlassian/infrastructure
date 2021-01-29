package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.ssh.api.SshConnection
import net.jcip.annotations.ThreadSafe

/**
 * @since 4.18.0
 */
@ThreadSafe
interface JiraInstallation {

    /**
     * Installs Jira on [server].
     *
     * @param [ssh] connects to the [server]
     * @param [server] will host the Jira
     */
    fun install(
        ssh: SshConnection,
        server: TcpServer
    ): InstalledJira
}
