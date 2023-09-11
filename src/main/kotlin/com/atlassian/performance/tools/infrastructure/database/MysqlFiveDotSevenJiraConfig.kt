package com.atlassian.performance.tools.infrastructure.database

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.report.StaticReport
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.io.File
import java.nio.file.Files

/**
 * [docs](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-mysql-5-7-966063305.html#ConnectingJiraapplicationstoMySQL5.7-dbconnectionfields)
 */
class MysqlFiveDotSevenJiraConfig(
    private val mysql: TcpNode
) : PostInstallHook {

    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks,
        reports: Reports
    ) {
        val remoteConfig: RemotePath = jira.home.resolve("dbconfig.xml")
        reports.add(StaticReport(remoteConfig.path), jira)
        val config: String = renderConfig()
        val localConfig: File = Files.createTempFile("dbconfig", ".xml")
            .toFile()
            .also { it.writeText(config) }
        remoteConfig.upload(localConfig)
    }

    private fun renderConfig(): String {
        val configTemplate = javaClass.classLoader.getResourceAsStream("mysql-dbconfig.xml").use {
            it!!.bufferedReader().readText()
        }
        return configTemplate
            .replace("dbserver", mysql.privateIp)
            .replace(":3306", ":${mysql.port}")

    }
}
