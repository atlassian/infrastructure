package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.PreStartHooks
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PostInstallHooks {

    private val hooks: Queue<PostInstallHook> = ConcurrentLinkedQueue()
    val preStart = PreStartHooks()
    val postStart = preStart.postStart
    val reports = postStart.reports

    fun insert(
        hook: PostInstallHook
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
