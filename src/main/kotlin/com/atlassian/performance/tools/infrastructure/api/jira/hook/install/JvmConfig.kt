package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.JiraGcLog
import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.SetenvSh
import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.FileListing
import com.atlassian.performance.tools.ssh.api.SshConnection

class JvmConfig(
    private val config: JiraNodeConfig
) : PostInstallHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    ) {
        val gcLog = JiraGcLog(jira.installation)
        SetenvSh(jira.installation).setup(
            connection = ssh,
            config = config,
            gcLog = gcLog,
            jiraIp = jira.server.ip
        )
        val report = FileListing(gcLog.path("*"))
        hooks.addReport(report)
    }
}
