package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHook
import com.atlassian.performance.tools.ssh.api.SshConnection

class DatabaseIpConfig(
    private val databaseIp: String
) : InstalledJiraHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: JiraNodeFlow
    ) {
        Sed().replace(
            connection = ssh,
            expression = "(<url>.*(@(//)?|//))" + "([^:/]+)" + "(.*</url>)",
            output = """\1$databaseIp\5""",
            file = "${jira.home}/dbconfig.xml"
        )
    }
}
