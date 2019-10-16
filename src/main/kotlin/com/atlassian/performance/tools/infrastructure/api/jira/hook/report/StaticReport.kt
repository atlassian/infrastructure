package com.atlassian.performance.tools.infrastructure.api.jira.hook.report

import com.atlassian.performance.tools.ssh.api.SshConnection

class StaticReport(
    private val remotePath: String
) : Report {

    override fun locate(ssh: SshConnection): List<String> = listOf(remotePath)
}
