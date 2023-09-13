package com.atlassian.performance.tools.infrastructure.database

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration.ofSeconds

/**
 * [docs](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-mysql-5-7-966063305.html#ConnectingJiraapplicationstoMySQL5.7-driver)
 */
class MysqlFiveConnector : PostInstallHook {

    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks,
        reports: Reports
    ) {
        val connector = "https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.40.tar.gz"
        IdempotentAction("Download MySQL connector") {
            ssh.execute("wget -q $connector")
        }.retry(3, StaticBackoff(ofSeconds(5)))
        ssh.execute("tar -xzf mysql-connector-java-5.1.40.tar.gz")
        ssh.execute("cp mysql-connector-java-5.1.40/mysql-connector-java-5.1.40-bin.jar ${jira.installation.path}/lib")
    }
}
