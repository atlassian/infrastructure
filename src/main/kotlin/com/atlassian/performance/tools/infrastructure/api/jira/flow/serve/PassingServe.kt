package com.atlassian.performance.tools.infrastructure.api.jira.flow.serve

import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.ssh.api.SshConnection

class PassingServe(
    private val report: Report
) : Serve {
    override fun serve(ssh: SshConnection, jira: StartedJira): Report = report
}
