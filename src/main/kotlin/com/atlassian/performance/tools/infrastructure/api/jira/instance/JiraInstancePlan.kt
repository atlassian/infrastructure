package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports

interface JiraInstancePlan {
    fun materialize(): JiraInstance
    fun report(): Reports
}