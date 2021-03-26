package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Intercepts a call before Jira is started.
 */
interface PreStartHook {

    /**
     * @param [ssh] connects to the [jira]
     * @param [jira] points to the installed Jira
     * @param [hooks] inserts future hooks
     * @param [reports] accumulates reports
     */
    fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PreStartHooks,
        reports: Reports
    )
}
