package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Intercepts a call before Jira is installed.
 */
interface PreInstallHook {

    /**
     * @param [ssh] connects to the [host]
     * @param [host] will install Jira
     * @param [hooks] inserts future hooks and reports
     */
    fun call(
            ssh: SshConnection,
            host: TcpHost,
            hooks: PreInstallHooks
    )
}
