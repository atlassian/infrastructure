package com.atlassian.performance.tools.infrastructure.hookapi.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports

interface PostInstanceHook {

    /**
     * @param [instance] a standalone Jira Server node or a Jira Data Center cluster
     * @param [hooks] inserts future hooks
     * @param [reports] accumulates reports
     */
    fun call(
        instance: JiraInstance,
        hooks: PostInstanceHooks,
        reports: Reports
    )
}
