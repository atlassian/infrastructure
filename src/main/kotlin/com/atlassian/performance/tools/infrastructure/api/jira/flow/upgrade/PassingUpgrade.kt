package com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.PassingServe
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.ssh.api.SshConnection

class PassingUpgrade(
    private val report: Report
) : Upgrade {

    override fun upgrade(ssh: SshConnection, jira: StartedJira): Serve = PassingServe(report)
}
