package com.atlassian.performance.tools.infrastructure.hookapi.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PostInstanceHooks private constructor() {

    private val hooks: Queue<PostInstanceHook> = ConcurrentLinkedQueue()

    fun insert(hook: PostInstanceHook) {
        hooks.add(hook)
    }

    fun call(instance: JiraInstance, reports: Reports) {
        while (true) {
            hooks
                .poll()
                ?.call(instance, this, reports)
                ?: break
        }
    }


    companion object Factory {
        fun default(): PostInstanceHooks = empty()
        fun empty(): PostInstanceHooks = PostInstanceHooks()
    }
}
