package com.atlassian.performance.tools.infrastructure.api.jira.hook

import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.Report
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class Reports protected constructor() {
    private val reports: Queue<Report> = ConcurrentLinkedQueue()

    fun addReport(
        report: Report
    ) {
        reports.add(report)
    }

    internal fun allReports(): Iterable<Report> {
        return reports.toList()
    }
}
