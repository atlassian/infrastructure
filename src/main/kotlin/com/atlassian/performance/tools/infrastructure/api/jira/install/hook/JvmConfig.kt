package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.JiraGcLog
import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.SetenvSh
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.FileListing
import com.atlassian.performance.tools.ssh.api.SshConnection

class JvmConfig(
    private val config: JiraNodeConfig
) : PostInstallHook {

    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    ) {
        val gcLog = JiraGcLog(jira.installation.path)
        SetenvSh(jira.installation.path).setup(
            connection = ssh,
            config = config,
            gcLog = gcLog,
            jiraIp = jira.host.ip
        )
        val report = FileListing(gcLog.path("*"))
        hooks.reports.add(report)
    }
}
