package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostInstallFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.ssh.api.SshConnection

class DatabaseIpConfig(
    private val databaseIp: String
) : PostInstallHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: PostInstallFlow
    ) {
        Sed().replace(
            connection = ssh,
            expression = "(<url>.*(@(//)?|//))" + "([^:/]+)" + "(.*</url>)",
            output = """\1$databaseIp\5""",
            file = "${jira.home}/dbconfig.xml"
        )
    }
}
