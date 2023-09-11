package com.atlassian.performance.tools.infrastructure.hookapi.database

import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class DatabaseIpConfig(
    private val databaseIp: String
) : PostInstallHook {

    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks,
        reports: Reports
    ) {
        val dbConfigXml = jira.home.resolve("dbconfig.xml").path
        reports.add(StaticReport(dbConfigXml), jira)
        Sed().replace(
            connection = ssh,
            expression = "(<url>.*(@(//)?|//))" + "([^:/]+)" + "(.*</url>)",
            output = """\1$databaseIp\5""",
            file = dbConfigXml
        )
    }
}
