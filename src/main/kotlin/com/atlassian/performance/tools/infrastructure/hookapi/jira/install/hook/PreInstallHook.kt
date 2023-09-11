package com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Intercepts a call before Jira is installed.
 */
interface PreInstallHook {

    /**
     * @param [ssh] connects to the [http] host
     * @param [http] will install Jira
     * @param [hooks] inserts future hooks
     * @param [reports] accumulates reports
     */
    fun call(
        ssh: SshConnection,
        http: HttpNode,
        hooks: PreInstallHooks,
        reports: Reports
    )
}
