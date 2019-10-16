package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostStartFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultPostStartHook : PostStartHook {

    override fun run(
        ssh: SshConnection,
        jira: StartedJira,
        flow: PostStartFlow
    ) {
        listOf(
            JstatHook()
        ).forEach { it.run(ssh, jira, flow) }
    }
}
