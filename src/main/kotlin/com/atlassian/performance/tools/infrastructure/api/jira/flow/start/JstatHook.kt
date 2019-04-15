package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JstatHook : StartedJiraHook {

    override fun hook(
        ssh: SshConnection,
        jira: StartedJira,
        flow: JiraNodeFlow
    ) {
        val process = jira.installed.jdk.jstatMonitoring.start(ssh, jira.pid)
        flow.reports.add(RemoteMonitoringProcessReport(process))
    }
}
