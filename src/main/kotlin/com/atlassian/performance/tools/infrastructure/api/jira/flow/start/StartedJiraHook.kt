package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

interface StartedJiraHook {
    fun hook(
        ssh: SshConnection,
        jira: StartedJira,
        flow: JiraNodeFlow
    )
}
