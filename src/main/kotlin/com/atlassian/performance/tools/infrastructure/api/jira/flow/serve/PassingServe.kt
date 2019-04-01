package com.atlassian.performance.tools.infrastructure.api.jira.flow.serve

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report

class PassingServe(
    private val report: Report
) : Serve {
    override fun report(): Report = report
}
