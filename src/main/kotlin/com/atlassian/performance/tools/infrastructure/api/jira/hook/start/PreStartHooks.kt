package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PreStartHooks {

    private val hooks: Queue<PreStartHook> = ConcurrentLinkedQueue()
    val postStart = PostStartHooks()
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
}
