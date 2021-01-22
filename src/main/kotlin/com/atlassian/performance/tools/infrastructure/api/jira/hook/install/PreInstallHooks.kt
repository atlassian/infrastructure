package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PreInstallHooks {

    private val hooks: Queue<PreInstallHook> = ConcurrentLinkedQueue()
    val postInstall = PostInstallHooks()
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
}
