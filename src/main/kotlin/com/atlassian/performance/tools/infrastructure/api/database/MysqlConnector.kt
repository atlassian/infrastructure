package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHook
import com.atlassian.performance.tools.jvmtasks.api.Backoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class MysqlConnector : InstalledJiraHook {

    override fun hook(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: JiraNodeFlow
    ) {
        val connector = "https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.40.tar.gz"
        IdempotentAction(
            description = "Download MySQL connector",
            action = { ssh.execute("wget -q $connector") }
        ).retry(
            maxAttempts = 3,
            backoff = StaticBackoff(Duration.ofSeconds(5))
        )
        ssh.execute("tar -xzf mysql-connector-java-5.1.40.tar.gz")
        ssh.execute("cp mysql-connector-java-5.1.40/mysql-connector-java-5.1.40-bin.jar ${jira.installation}/lib")
    }
}

private class StaticBackoff(
    private val backOff: Duration
) : Backoff {
    override fun backOff(attempt: Int): Duration = backOff
}
