package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PreStartHooks private constructor(
    val postStart: PostStartHooks
) {

    private val hooks: Queue<PreStartHook> = ConcurrentLinkedQueue()
    val reports = postStart.reports

    fun insert(
        hook: PreStartHook
    ) {
        hooks.add(hook)
    }

    internal fun call(
        ssh: SshConnection,
        jira: InstalledJira
    ) {
        while (true) {
            hooks
                .poll()
                ?.call(ssh, jira, this)
                ?: break
        }
    }

    companion object Factory {
        fun default() = PreStartHooks(PostStartHooks.default())

        fun empty() = PreStartHooks(PostStartHooks.empty())
    }
}
