package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class PostStartFlow protected constructor() : Reports() {

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
