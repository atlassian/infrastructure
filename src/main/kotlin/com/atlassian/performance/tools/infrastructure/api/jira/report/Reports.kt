package com.atlassian.performance.tools.infrastructure.api.jira.report

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class Reports {
    private val reports: Queue<Report> = ConcurrentLinkedQueue()

    fun add(
        report: Report
    ) {
        reports.add(report)
    }

    fun list(): Iterable<Report> {
        return reports.toList()
    }
}
