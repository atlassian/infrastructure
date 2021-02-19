package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PostStartHooks private constructor() {

    private val hooks: Queue<PostStartHook> = ConcurrentLinkedQueue()
    val reports = Reports()

    fun insert(
        hook: PostStartHook
    ) {
        hooks.add(hook)
    }

    internal fun call(
        ssh: SshConnection,
        jira: StartedJira
    ) {
        while (true) {
            hooks
                .poll()
                ?.call(ssh, jira, this)
                ?: break
        }
    }

    companion object Factory {
        fun default(): PostStartHooks = empty().apply {
            insert(JstatHook())
        }

        fun empty(): PostStartHooks = PostStartHooks()
    }
}
