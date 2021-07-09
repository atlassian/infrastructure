package com.atlassian.performance.tools.infrastructure.jira.report

import com.atlassian.performance.tools.infrastructure.api.jira.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class JiraLandingPage(
    private val started: StartedJira
) : Report {
    override fun locate(ssh: SshConnection): List<String> {
        Ubuntu().install(ssh, listOf("curl"))
        val landingPage = started.installed.http.addressPrivately()
        val html = "jira-landing-page.html"
        val headers = "jira-landing-page-headers.txt"
        ssh.execute("curl $landingPage --location --output $html --dump-header $headers --silent")
        return listOf(html, headers)
    }
}
