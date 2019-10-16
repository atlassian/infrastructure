package com.atlassian.performance.tools.infrastructure.api.jira.hook.report

import com.atlassian.performance.tools.ssh.api.SshConnection

class FileListing(
    private val pattern: String
) : Report {
    override fun locate(
        ssh: SshConnection
    ): List<String> = ssh
        .execute("ls $pattern")
        .output
        .lines()
        .filter { it.isNotBlank() }
}
