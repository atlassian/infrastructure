package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.JiraGcLog
import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.SetenvSh
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.ssh.api.SshConnection

class JvmConfig(
    private val config: JiraNodeConfig
) : InstalledJiraHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: JiraNodeFlow
    ) {
        val gcLog = JiraGcLog(jira.installation)
        SetenvSh(jira.installation).setup(
            connection = ssh,
            config = config,
            gcLog = gcLog,
            jiraIp = jira.server.ip
        )
        val report = FileListing(gcLog.path("*"))
        flow.reports.add(report)
    }
}
