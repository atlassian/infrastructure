package com.atlassian.performance.tools.infrastructure.api.jira.hook

import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.PostStartHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class PostStartHooks protected constructor() : Reports() {

    private val postStartHooks: Queue<PostStartHook> = ConcurrentLinkedQueue()

    fun hook(
        hook: PostStartHook
    ) {
        postStartHooks.add(hook)
    }

    internal fun runPostStartHooks(
        ssh: SshConnection,
        jira: StartedJira
    ) {
        while (true) {
            postStartHooks
                .poll()
                ?.run(ssh, jira, this)
                ?: break
        }
    }
}
