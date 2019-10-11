package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.nio.file.Path
import java.nio.file.Paths

class JiraLogs : InstalledJiraHook {

    override fun run(ssh: SshConnection, jira: InstalledJira, flow: JiraNodeFlow) {
        listOf(
            "${jira.home}/log/atlassian-jira.log",
            "${jira.installation}/logs/catalina.out"
        )
            .onEach { ensureFile(Paths.get(it), ssh) }
            .map { StaticReport(it) }
            .forEach { flow.reports.add(it) }
    }

    private fun ensureFile(
        path: Path,
        ssh: SshConnection
    ) {
        ssh.execute("mkdir -p ${path.parent!!}")
        ssh.execute("touch $path")
    }
}
