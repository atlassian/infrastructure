package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpServer
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PreInstallHooks private constructor(
    val postInstall: PostInstallHooks
) {

    private val hooks: Queue<PreInstallHook> = ConcurrentLinkedQueue()
    val preStart = postInstall.preStart
    val postStart = preStart.postStart
    val reports = postStart.reports

    fun insert(
        hook: PreInstallHook
    ) {
        hooks.add(hook)
    }

    internal fun call(
        ssh: SshConnection,
        server: TcpServer
    ) {
        while (true) {
            hooks
                .poll()
                ?.call(ssh, server, this)
                ?: break
        }
    }

    companion object Factory {
        fun default(): PreInstallHooks = PreInstallHooks(PostInstallHooks.default())

        fun empty(): PreInstallHooks = PreInstallHooks(PostInstallHooks.empty())
    }
}
