package com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.nio.file.Path
import java.nio.file.Paths

class JiraLogs : PostInstallHook {

    override fun call(ssh: SshConnection, jira: InstalledJira, hooks: PostInstallHooks, reports: Reports) {
        reports.add(report(jira), jira)
    }

    fun report(jira: InstalledJira): Report {
        return JiraLogsReport(jira)
    }

    private class JiraLogsReport(private val jira: InstalledJira) : Report {
        override fun locate(ssh: SshConnection): List<String> {
            return listOf(
                "${jira.home.path}/log/atlassian-jira.log",
                "${jira.installation.path}/logs/catalina.out"
            ).onEach { ensureFile(Paths.get(it), ssh) }
        }

        private fun ensureFile(path: Path, ssh: SshConnection) {
            ssh.execute("mkdir -p ${path.parent!!}")
            ssh.execute("touch $path")
        }
    }
}
