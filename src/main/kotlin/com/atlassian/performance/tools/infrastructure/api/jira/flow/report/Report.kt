package com.atlassian.performance.tools.infrastructure.api.jira.flow.report

import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Locates local serve files after [Serve].
 */
interface Report {
    fun locate(ssh: SshConnection): List<String>
}
