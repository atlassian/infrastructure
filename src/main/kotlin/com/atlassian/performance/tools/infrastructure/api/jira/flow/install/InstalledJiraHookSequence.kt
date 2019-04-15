package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.ssh.api.SshConnection

class InstalledJiraHookSequence(
    private val hooks: List<InstalledJiraHook>
) : InstalledJiraHook {

    override fun hook(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: JiraNodeFlow
    ) {
        hooks.forEach { it.hook(ssh, jira, flow) }
    }
}
