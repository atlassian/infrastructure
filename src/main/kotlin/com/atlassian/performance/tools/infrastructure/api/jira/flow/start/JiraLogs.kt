package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.PassingUpgrade
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.ssh.api.SshConnection

class JiraLogs : Start {
    override fun start(
        ssh: SshConnection,
        jira: InstalledJira
    ): Upgrade {
        return PassingUpgrade(StaticReport(listOf(
            "${jira.home}/log/atlassian-jira.log",
            "${jira.installation}/logs/catalina.out",
            "${jira.installation}/logs/*access*"
        )))
    }
}
