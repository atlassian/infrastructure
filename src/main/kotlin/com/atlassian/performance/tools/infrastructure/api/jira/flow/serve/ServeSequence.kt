package com.atlassian.performance.tools.infrastructure.api.jira.flow.serve

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportSequence

class ServeSequence(
    private val serves: List<Serve>
) : Serve {
    override fun report(): Report {
        return ReportSequence(serves.map { it.report() })
    }
}
