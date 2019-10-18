package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PreInstanceHooks(
    val nodes: List<PreInstallHooks>
) {

    private val hooks: Queue<PreInstanceHook> = ConcurrentLinkedQueue()
    val postInstance = PostInstanceHooks(nodes)

    fun hook(hook: PreInstanceHook) {
        hooks.add(hook)
    }

    fun call() {
        while (true) {
            hooks
                .poll()
                ?.call(this)
                ?: break
        }
    }
}
