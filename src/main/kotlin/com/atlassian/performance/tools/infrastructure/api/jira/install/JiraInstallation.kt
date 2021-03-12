package com.atlassian.performance.tools.infrastructure.api.jira.install

import net.jcip.annotations.ThreadSafe

/**
 * @since 4.18.0
 */
@ThreadSafe
interface JiraInstallation {

    /**
     * Installs Jira on [host].
     *
     * @param [host] will host the Jira
     */
    fun install(
            host: TcpHost
    ): InstalledJira
}
