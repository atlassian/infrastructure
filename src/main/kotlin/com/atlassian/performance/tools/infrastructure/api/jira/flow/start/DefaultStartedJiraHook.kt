package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.JiraLaunchTimeouts
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.JiraLogs
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultStartedJiraHook : StartedJiraHook {

    override fun hook(
        ssh: SshConnection,
        jira: StartedJira,
        flow: JiraNodeFlow
    ) {
        listOf(
            JiraLogs(),
            JstatHook(),
            RestUpgrade(
                JiraLaunchTimeouts.Builder().build(),
                "admin",
                "admin"
            )
        ).forEach { it.hook(ssh, jira, flow) }
    }
}
