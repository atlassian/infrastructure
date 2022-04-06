package com.atlassian.performance.tools.infrastructure.api.jira.start

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import net.jcip.annotations.ThreadSafe

/**
 * @since 4.19.0
 */
@ThreadSafe
interface JiraStart {

    /**
     * Starts the [installed] Jira.
     *
     * @param [installed] will start the Jira
     * @param [reports] accumulates reports
     */
    fun start(
        installed: InstalledJira,
        reports: Reports
    ): StartedJira
}
