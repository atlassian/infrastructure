package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostInstallFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.nio.file.Path
import java.nio.file.Paths

class JiraLogs : PostInstallHook {

    override fun run(ssh: SshConnection, jira: InstalledJira, flow: PostInstallFlow) {
        listOf(
            "${jira.home}/log/atlassian-jira.log",
            "${jira.installation}/logs/catalina.out"
        )
            .onEach { ensureFile(Paths.get(it), ssh) }
            .map { StaticReport(it) }
            .forEach { flow.addReport(it) }
    }

    private fun ensureFile(
        path: Path,
        ssh: SshConnection
    ) {
        ssh.execute("mkdir -p ${path.parent!!}")
        ssh.execute("touch $path")
    }
}
