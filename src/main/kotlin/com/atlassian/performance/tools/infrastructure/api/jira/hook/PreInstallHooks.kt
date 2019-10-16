package com.atlassian.performance.tools.infrastructure.api.jira.hook

import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.PreInstallHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class PreInstallHooks protected constructor() : PostInstallHooks() {

    private val preInstallHooks: Queue<PreInstallHook> = ConcurrentLinkedQueue()

    fun hook(
        hook: PreInstallHook
    ) {
        preInstallHooks.add(hook)
    }

    internal fun runPreInstallHooks(
        ssh: SshConnection,
        server: TcpServer
    ) {
        while (true) {
            preInstallHooks
                .poll()
                ?.run(ssh, server, this)
                ?: break
        }
    }
}
