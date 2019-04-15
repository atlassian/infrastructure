package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.JiraLaunchTimeouts
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jvm.ThreadDump
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration
import java.time.Instant

class RestUpgrade(
    private val timeouts: JiraLaunchTimeouts,
    private val adminUsername: String,
    private val adminPassword: String
) : StartedJiraHook {

    override fun hook(ssh: SshConnection, jira: StartedJira, flow: JiraNodeFlow) {
        val threadDump = ThreadDump(jira.pid, jira.installed.jdk)
        val privatePort = jira.installed.server.privatePort
        val upgradesEndpoint = URI("http://$adminUsername:$adminPassword@localhost:$privatePort/rest/api/2/upgrade")
        flow.reports.add(StaticReport("thread-dumps"))
        waitForStatusToChange(
            statusQuo = "000",
            timeout = timeouts.offlineTimeout,
            ssh = ssh,
            uri = upgradesEndpoint,
            threadDump = threadDump
        )
        waitForStatusToChange(
            statusQuo = "503",
            timeout = timeouts.initTimeout,
            ssh = ssh,
            uri = upgradesEndpoint,
            threadDump = threadDump
        )
        ssh.execute(
            cmd = "curl --silent --retry 6 -X POST $upgradesEndpoint",
            timeout = Duration.ofSeconds(15)
        )
        waitForStatusToChange(
            statusQuo = "303",
            timeout = timeouts.upgradeTimeout,
            ssh = ssh,
            uri = upgradesEndpoint,
            threadDump = threadDump
        )
    }

    private fun waitForStatusToChange(
        statusQuo: String,
        uri: URI,
        timeout: Duration,
        ssh: SshConnection,
        threadDump: ThreadDump
    ) {
        val backoff = Duration.ofSeconds(10)
        val deadline = Instant.now() + timeout
        while (true) {
            val currentStatus = ssh.safeExecute(
                cmd = "curl --silent --write-out '%{http_code}' --output /dev/null -X GET $uri",
                timeout = timeouts.unresponsivenessTimeout
            ).output
            if (currentStatus != statusQuo) {
                break
            }
            if (deadline < Instant.now()) {
                throw Exception("$uri failed to get out of $statusQuo status within $timeout")
            }
            threadDump.gather(ssh, "thread-dumps")
            Thread.sleep(backoff.toMillis())
        }
    }
}
