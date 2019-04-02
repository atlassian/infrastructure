package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.ssh.api.SshConnection

class ReportableJira(
    private val reportHook: Report
) {
    fun report(
        ssh: SshConnection
    ): List<String> {
        return reportHook.locate(ssh)
    }
}
