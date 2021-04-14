package com.atlassian.performance.tools.infrastructure.jira.install

import net.jcip.annotations.ThreadSafe

@ThreadSafe
interface JiraInstallation {

    /**
     * Installs Jira on [server].
     *
     * @param [server] will host the Jira
     */
    fun install(
        server: TcpServer
    ): InstalledJira
}
