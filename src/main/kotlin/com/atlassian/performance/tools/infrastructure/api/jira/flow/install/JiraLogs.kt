package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JiraLogs : InstalledJiraHook {

    override fun hook(ssh: SshConnection, jira: InstalledJira, flow: JiraNodeFlow) {
        listOf(
            StaticReport("${jira.home}/log/atlassian-jira.log"),
            StaticReport("${jira.installation}/logs/catalina.out")
        ).forEach { flow.reports.add(it) }
    }
}
