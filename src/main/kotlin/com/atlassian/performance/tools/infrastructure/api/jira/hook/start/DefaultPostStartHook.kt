package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultPostStartHook : PostStartHook {

    override fun run(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks
    ) {
        listOf(
            JstatHook()
        ).forEach { it.run(ssh, jira, hooks) }
    }
}
