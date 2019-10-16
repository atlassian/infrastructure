package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.nio.file.Path
import java.nio.file.Paths

class JiraLogs : PostInstallHook {

    override fun run(ssh: SshConnection, jira: InstalledJira, hooks: PostInstallHooks) {
        listOf(
            "${jira.home}/log/atlassian-jira.log",
            "${jira.installation}/logs/catalina.out"
        )
            .onEach { ensureFile(Paths.get(it), ssh) }
            .map { StaticReport(it) }
            .forEach { hooks.addReport(it) }
    }

    private fun ensureFile(
        path: Path,
        ssh: SshConnection
    ) {
        ssh.execute("mkdir -p ${path.parent!!}")
        ssh.execute("touch $path")
    }
}
