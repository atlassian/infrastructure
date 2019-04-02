package com.atlassian.performance.tools.infrastructure.api.jira.flow.serve

import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportSequence
import com.atlassian.performance.tools.ssh.api.SshConnection

class ServeSequence(
    private val serves: List<Serve>
) : Serve {
    override fun serve(ssh: SshConnection, jira: StartedJira): Report {
        return ReportSequence(serves.map { it.serve(ssh, jira) })
    }
}
