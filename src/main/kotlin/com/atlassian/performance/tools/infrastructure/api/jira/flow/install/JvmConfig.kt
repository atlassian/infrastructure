package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.JiraGcLog
import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.SetenvSh
import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PassingStart
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.ssh.api.SshConnection

class JvmConfig(
    private val config: JiraNodeConfig
) : Install {
    override fun install(
        ssh: SshConnection,
        jira: InstalledJira
    ): Start {
        val gcLog = JiraGcLog(jira.installation)
        SetenvSh(jira.installation).setup(
            connection = ssh,
            config = config,
            gcLog = gcLog,
            jiraIp = jira.name
        )
        return PassingStart(FileListing(gcLog.path("*")))
    }
}
