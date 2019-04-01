package com.atlassian.performance.tools.infrastructure.api.jira.flow.report

import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Locates local report files after [Serve].
 */
interface Report {
    fun locate(ssh: SshConnection): List<String>
}