package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report

interface Reports {
    fun addReport(report: Report): Reports
}
