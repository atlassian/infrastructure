package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.JiraLaunchTimeouts
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.JiraLogs
import com.atlassian.performance.tools.infrastructure.api.jira.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jvm.ThreadDump
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration
import java.time.Instant

class RestUpgrade(
    private val timeouts: JiraLaunchTimeouts,
    private val adminUsername: String,
    private val adminPassword: String
) : PostStartHook {

    override fun call(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks, reports: Reports) {
        val threadDump = ThreadDump(jira.pid, jira.installed.jdk)
        val polling = Upgrades(ssh, jira, adminUsername, adminPassword, timeouts, threadDump, reports)
        polling.waitUntilOnline()
        polling.waitUntilHealthy()
        polling.triggerUpgrades()
        polling.waitUntilUpgraded()
    }

    private class Upgrades(
        private val ssh: SshConnection,
        private val jira: StartedJira,
        adminUsername: String,
        adminPassword: String,
        private val timeouts: JiraLaunchTimeouts,
        private val threadDump: ThreadDump,
        private val reports: Reports
    ) {
        private val upgradesEndpoint: URI = jira
            .installed
            .http
            .addressPrivately(adminUsername, adminPassword)
            .resolve("rest/api/2/upgrade")

        fun waitUntilOnline() {
            waitForStatusToChange("000", timeouts.offlineTimeout)
        }

        fun waitUntilHealthy() {
            waitForStatusToChange("503", timeouts.initTimeout)
        }

        fun waitUntilUpgraded() {
            waitForStatusToChange("303", timeouts.upgradeTimeout)
        }

        private fun waitForStatusToChange(
            statusQuo: String,
            timeout: Duration
        ) {
            val backoff = Duration.ofSeconds(10)
            val deadline = Instant.now() + timeout
            while (true) {
                val currentStatus = ssh.safeExecute(
                    cmd = "curl --silent --write-out '%{http_code}' --output /dev/null -X GET $upgradesEndpoint",
                    timeout = timeouts.unresponsivenessTimeout
                ).output
                if (currentStatus != statusQuo) {
                    break
                }
                if (deadline < Instant.now()) {
                    reports.add(JiraLogs().report(jira.installed), jira)
                    reports.add(FileListing("thread-dumps/*"), jira)
                    throw Exception("$upgradesEndpoint failed to get out of $statusQuo status within $timeout")
                }
                threadDump.gather(ssh, "thread-dumps")
                Thread.sleep(backoff.toMillis())
            }
        }

        fun triggerUpgrades() {
            ssh.execute(
                cmd = "curl --silent --retry 6 -X POST $upgradesEndpoint",
                timeout = Duration.ofSeconds(15)
            )
        }
    }
}