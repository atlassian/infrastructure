package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Intercepts a call after Jira is started.
 */
interface PostStartHook {

    /**
     * @param [ssh] connects to the [jira]
     * @param [jira] points to the started Jira
     * @param [hooks] inserts future hooks
     * @param [reports] accumulates reports
     */
    fun call(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks,
        reports: Reports
    )
}
