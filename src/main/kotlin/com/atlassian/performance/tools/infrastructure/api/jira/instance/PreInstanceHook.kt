package com.atlassian.performance.tools.infrastructure.api.jira.instance

interface PreInstanceHook {

    /**
     * @param [hooks] inserts future hooks and reports
     */
    fun call(
        hooks: PreInstanceHooks
    )
}
