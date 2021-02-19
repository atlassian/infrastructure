package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.nio.file.Path
import java.nio.file.Paths

class JiraLogs : PostInstallHook {

    override fun call(ssh: SshConnection, jira: InstalledJira, hooks: PostInstallHooks) {
        listOf(
            "${jira.home.path}/log/atlassian-jira.log",
            "${jira.installation.path}/logs/catalina.out"
        )
            .onEach { ensureFile(Paths.get(it), ssh) }
            .map { StaticReport(it) }
            .forEach { hooks.reports.add(it) }
    }

    private fun ensureFile(
        path: Path,
        ssh: SshConnection
    ) {
        ssh.execute("mkdir -p ${path.parent!!}")
        ssh.execute("touch $path")
    }
}
