package com.atlassian.performance.tools.infrastructure.api.jira.flow.report

import com.atlassian.performance.tools.ssh.api.SshConnection

class ReportSequence(
    private val reports: List<Report>
) : Report {
    override fun locate(ssh: SshConnection): List<String> = reports.flatMap { it.locate(ssh) }
}
