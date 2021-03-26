package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
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
     * @param [reports] accumulates reports
     */
    fun install(
        host: TcpHost,
        reports: Reports
    ): InstalledJira
}
