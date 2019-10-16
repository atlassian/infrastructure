package com.atlassian.performance.tools.infrastructure.api.jira.hook

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class PreStartHooks protected constructor() : PostStartHooks() {
    private val preStartHooks: Queue<PreStartHook> = ConcurrentLinkedQueue()

    fun hook(
        hook: PreStartHook
    ) {
        preStartHooks.add(hook)
    }

    internal fun runPreStartHooks(
        ssh: SshConnection,
        jira: InstalledJira
    ) {
        while (true) {
            preStartHooks
                .poll()
                ?.run(ssh, jira, this)
                ?: break
        }
    }
}
