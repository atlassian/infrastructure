package com.atlassian.performance.tools.infrastructure.api.jira.instance

import java.net.URI

interface PostInstanceHook {

    /**
     * @param [instance] a standalone Jira Server node or a Jira Data Center cluster
     * @param [hooks] inserts future hooks and reports
     */
    fun call(
        instance: URI,
        hooks: PostInstanceHooks
    )
}
