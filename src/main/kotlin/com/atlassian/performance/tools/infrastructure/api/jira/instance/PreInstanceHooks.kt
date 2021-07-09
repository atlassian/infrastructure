package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PreInstanceHooks private constructor(
    val postInstance: PostInstanceHooks
) {

    private val hooks: Queue<PreInstanceHook> = ConcurrentLinkedQueue()

    fun insert(hook: PreInstanceHook) {
        hooks.add(hook)
    }

    internal fun call(nodes: List<PreInstallHooks>, reports: Reports) {
        while (true) {
            hooks
                .poll()
                ?.call(nodes, this, reports)
                ?: break
        }
    }

    companion object Factory {
        fun default(): PreInstanceHooks = PreInstanceHooks(PostInstanceHooks.default())
        fun empty(): PreInstanceHooks = PreInstanceHooks(PostInstanceHooks.empty())
    }
}
