package com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade

import com.atlassian.performance.tools.infrastructure.api.jira.JiraLaunchTimeouts
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.PassingServe
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.infrastructure.api.jvm.ThreadDump
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration
import java.time.Instant

class RestUpgrade(
    private val timeouts: JiraLaunchTimeouts,
    private val adminUsername: String,
    private val adminPassword: String
) : Upgrade {

    override fun upgrade(ssh: SshConnection, jira: StartedJira): Serve {
        val threadDump = ThreadDump(jira.pid, jira.installed.jdk)
        val upgradesEndpoint = URI("http://$adminUsername:$adminPassword@localhost:8080/rest/api/2/upgrade")
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
        return PassingServe(StaticReport("thread-dumps"))
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
