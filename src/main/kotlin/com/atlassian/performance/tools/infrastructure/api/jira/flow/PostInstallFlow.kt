package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class PostInstallFlow protected constructor() : PreStartFlow() {

    private val postInstallHooks: Queue<PostInstallHook> = ConcurrentLinkedQueue()

    fun hook(
        hook: PostInstallHook
    ) {
        postInstallHooks.add(hook)
    }

    internal fun runPostInstallHooks(
        ssh: SshConnection,
        jira: InstalledJira
    ) {
        while (true) {
            postInstallHooks
                .poll()
                ?.run(ssh, jira, this)
                ?: break
        }
    }

}
