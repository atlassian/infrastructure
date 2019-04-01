package com.atlassian.performance.tools.infrastructure.api.jira.flow.serve

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report

/**
 * Hooks in before Jira starts serving to clients after [Upgrade].
 */
interface Serve {

    fun report(): Report
}