package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PreInstallHooks private constructor(
    val postInstall: PostInstallHooks
) {

    private val hooks: Queue<PreInstallHook> = ConcurrentLinkedQueue()
    val preStart = postInstall.preStart
    val postStart = preStart.postStart

    fun insert(
        hook: PreInstallHook
    ) {
        hooks.add(hook)
    }

    internal fun call(
        ssh: SshConnection,
        http: HttpNode,
        reports: Reports
    ) {
        while (true) {
            hooks
                .poll()
                ?.call(ssh, http, this, reports)
                ?: break
        }
    }

    companion object Factory {
        fun default(): PreInstallHooks = PreInstallHooks(PostInstallHooks.default()).apply {
            insert(SystemLog())
        }

        fun empty(): PreInstallHooks = PreInstallHooks(PostInstallHooks.empty())
    }
}
