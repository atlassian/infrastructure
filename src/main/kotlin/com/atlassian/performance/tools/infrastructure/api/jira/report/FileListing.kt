package com.atlassian.performance.tools.infrastructure.api.jira.report

import com.atlassian.performance.tools.ssh.api.SshConnection

class FileListing(
    private val pattern: String
) : Report {

    override fun locate(
        ssh: SshConnection
    ): List<String> = ssh
        .safeExecute("ls $pattern")
        .takeIf { it.isSuccessful() }
        ?.output
        ?.lines()
        ?.filter { it.isNotBlank() }
        ?: emptyList()
}
