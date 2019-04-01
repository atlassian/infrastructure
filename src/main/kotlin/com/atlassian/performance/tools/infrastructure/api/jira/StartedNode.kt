package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.ssh.api.Ssh
import java.time.Duration

class StartedNode(
    private val serve: Serve,
    private val analyticLogs: String,
    private val ssh: Ssh
) {
    private val resultsDirectory = "results"

    fun serve(): Report {
        return serve.report()
    }

    fun gatherAnalyticLogs() {
        ssh.newConnection().use {
            it.execute("cp -r $analyticLogs/analytics-logs $resultsDirectory")
            it.execute("find $resultsDirectory/analytics-logs/ -maxdepth 1 -type f -name '*.gz' -exec gunzip {} +")
            AwsCli().upload(
                location = resultsTransport.location,
                ssh = it,
                source = resultsDirectory,
                timeout = Duration.ofMinutes(2)
            )
        }
    }
}