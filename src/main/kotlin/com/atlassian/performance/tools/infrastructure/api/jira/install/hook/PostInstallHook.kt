package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Intercepts a call after Jira is installed.
 */
interface PostInstallHook {

    /**
     * @param [ssh] connects to the [jira]
     * @param [jira] points to the installed Jira
     * @param [hooks] inserts future hooks and reports
     */
    fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    )
}
