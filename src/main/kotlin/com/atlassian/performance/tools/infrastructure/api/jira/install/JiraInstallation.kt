package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import net.jcip.annotations.ThreadSafe

/**
 * @since 4.19.0
 */
@ThreadSafe
interface JiraInstallation {

    /**
     * Installs Jira on [http] node.
     *
     * @param [http] will host the Jira
     * @param [reports] accumulates reports
     */
    fun install(
        http: HttpNode,
        reports: Reports
    ): InstalledJira
}
