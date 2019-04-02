package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportSequence
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.PassingUpgrade
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.ssh.api.SshConnection

class JiraLogs : Start {
    override fun start(
        ssh: SshConnection,
        jira: InstalledJira
    ): Upgrade = PassingUpgrade(ReportSequence(listOf(
        StaticReport("${jira.home}/log/atlassian-jira.log"),
        StaticReport("${jira.installation}/logs/catalina.out"),
        FileListing("${jira.installation}/logs/*access*")
    )))
}
