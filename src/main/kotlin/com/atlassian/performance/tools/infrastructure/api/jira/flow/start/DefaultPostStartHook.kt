package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultPostStartHook : PostStartHook {

    override fun hook(
        ssh: SshConnection,
        jira: StartedJira,
        flow: JiraNodeFlow
    ) {
        listOf(
            JiraLogs(),
            JstatHook()
            //,RestUpgrade(config.launchTimeouts, "admin", "admin") TODO requires database to work
        ).forEach { it.hook(ssh, jira, flow) }
    }
}
