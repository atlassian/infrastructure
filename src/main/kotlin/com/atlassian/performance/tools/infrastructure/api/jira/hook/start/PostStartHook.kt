package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Intercepts a call after Jira is started.
 */
interface PostStartHook {

    /**
     * @param [ssh] connects to the [jira]
     * @param [jira] points to the started Jira
     * @param [hooks] inserts future reports
     */
    fun call(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks
    )
}
