package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports

interface PreInstanceHook {

    /**
     * @param [nodes] inserts node hooks
     * @param [hooks] inserts future hooks
     * @param [reports] accumulates reports
     */
    fun call(
        nodes: List<PreInstallHooks>,
        hooks: PreInstanceHooks,
        reports: Reports
    )
}
