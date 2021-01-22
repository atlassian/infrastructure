package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.PostStartHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PostStartHooks {

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
}
