package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JiraLogs : PreStartHook, PostStartHook {

    override fun hook(ssh: SshConnection, jira: InstalledJira, flow: JiraNodeFlow) {
        listOf(
            StaticReport("${jira.home}/log/atlassian-jira.log"),
            StaticReport("${jira.installation}/logs/catalina.out")
        ).forEach { flow.reports.add(it) }
    }

    override fun hook(ssh: SshConnection, jira: StartedJira, flow: JiraNodeFlow) {
        hook(ssh, jira.installed, flow)
    }
}
