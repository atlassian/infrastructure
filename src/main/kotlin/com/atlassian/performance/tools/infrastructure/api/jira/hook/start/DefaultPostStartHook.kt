package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultPostStartHook : PostStartHook {

    override fun call(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks
    ) {
        listOf(
            JstatHook()
        ).forEach { it.call(ssh, jira, hooks) }
    }
}
