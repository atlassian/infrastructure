package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PostInstanceHooks(
    val nodes: List<PreInstallHooks>
) {

    private val hooks: Queue<PostInstanceHook> = ConcurrentLinkedQueue()

    fun insert(hook: PostInstanceHook) {
        hooks.add(hook)
    }

    fun call(instance: URI) {
        while (true) {
            hooks
                .poll()
                ?.call(instance, this)
                ?: break
        }
    }
}
