package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.StartedJiraHook
import com.atlassian.performance.tools.ssh.api.SshConnection

interface InstalledJiraHook : StartedJiraHook {
    fun hook(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: JiraNodeFlow
    )

    override fun hook(
        ssh: SshConnection,
        jira: StartedJira,
        flow: JiraNodeFlow
    ) {
        hook(ssh, jira.installed, flow)
    }
}
