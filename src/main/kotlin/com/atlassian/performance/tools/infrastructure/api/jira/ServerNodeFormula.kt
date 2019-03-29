package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.jvmtasks.api.Backoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.TaskTimer
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

internal class ServerNodeFormula(
    private val jiraHomeSource: JiraHomeSource,
    private val hook: JiraNodeInstallationHook,
    private val resultsTransport: Storage,
    private val databaseIp: String,
    private val productDistribution: ProductDistribution,
    private val ssh: Ssh,
    private val config: JiraNodeConfig,
    private val dbType: DbType = DbType.MySql,
    private val adminUser: String = "admin",
    private val adminPwd: String = "admin"
) {
    private val jdk = config.jdk
    private val ubuntu: Ubuntu = Ubuntu()

    fun provision(): JiraNode {
        ssh.newConnection().use { connection ->
            val unpackedProduct = productDistribution.install(connection, ".")
            val jiraHome = TaskTimer.time("download Jira home") {
                jiraHomeSource.download(connection)
            }
            replaceDbconfigUrl(connection, "$jiraHome/dbconfig.xml")
            connection.execute("echo jira.home=`realpath $jiraHome` > $unpackedProduct/atlassian-jira/WEB-INF/classes/jira-application.properties")
            val bareJiraNode = BareJiraNode(
                connection,
                jiraHome,
                unpackedProduct,
                ssh.host.ipAddress
            )
            val results = hook.hook(bareJiraNode)

//            AwsCli().download(pluginsTransport.location, connection, target = "$home/plugins/installed-plugins")

            val osMetrics = ubuntu.metrics(connection)

            config.splunkForwarder.jsonifyLog4j(
                connection,
                log4jPropertiesPath = "$unpackedProduct/atlassian-jira/WEB-INF/classes/log4j.properties"
            )
            config.splunkForwarder.run(connection, name, "/home/ubuntu/jirahome/log")
            config.profiler.install(connection)

            return JiraNode(
                results = results,
                jiraHome = jiraHome,
                analyticLogs = jiraHome,
                resultsTransport = resultsTransport,
                unpackedProduct = unpackedProduct,
                osMetrics = osMetrics,
                ssh = ssh,
                launchTimeouts = config.launchTimeouts,
                jdk = jdk,
                profiler = config.profiler,
                adminUser = adminUser,
                adminPwd = adminPwd
            )
        }
    }

    private fun replaceDbconfigUrl(
        connection: SshConnection,
        dbconfigXml: String
    ) {
        Sed().replace(
            connection = connection,
            expression = "(<url>.*(@(//)?|//))" + "([^:/]+)" + "(.*</url>)",
            output = """\1$databaseIp\5""",
            file = dbconfigXml
        )
    }


}

private class StaticBackoff(
    private val backOff: Duration
) : Backoff {
    override fun backOff(attempt: Int): Duration = backOff
}

interface JiraNodeInstallationHook {

    fun hook(jira: BareJiraNode): RemoteResult
}

class JiraNodeInstallationHookChain(
    private val hooks: List<JiraNodeInstallationHook>
) : JiraNodeInstallationHook {

    override fun hook(jira: BareJiraNode): RemoteResult {
        val results = hooks.map { it.hook(jira) }
        return RemoteResultChain(results)
    }
}

interface RemoteResult {

    fun locate(): List<String>
}

class StaticRemoteResult(
    private val remotePaths: List<String>
) : RemoteResult {
    override fun locate(): List<String> = remotePaths
}

class EmptyRemoteResult : RemoteResult {
    override fun locate(): List<String> = emptyList()
}

class RemoteResultChain(
    private val results: List<RemoteResult>
) : RemoteResult {

    override fun locate(): List<String> {
        return results.flatMap { it.locate() }
    }
}

class BareJiraNode(
    val ssh: SshConnection,
    val home: String,
    val installation: String,
    val ip: String
)

class GcAndJmx(
    private val config: JiraNodeConfig
) : JiraNodeInstallationHook {

    override fun hook(
        jira: BareJiraNode
    ): RemoteResult {
        val gcLog = JiraGcLog(jira.installation)
        SetenvSh(jira.installation).setup(
            connection = jira.ssh,
            config = config,
            gcLog = gcLog,
            jiraIp = jira.ip
        )
        return StaticRemoteResult(listOf(gcLog.path()))
    }
}

class DisabledAutoBackup : JiraNodeInstallationHook {

    override fun hook(jira: BareJiraNode): RemoteResult {
        jira.ssh.execute("echo jira.autoexport=false > ${jira.home}/jira-config.properties")
        return EmptyRemoteResult()
    }
}

class MysqlConnector : JiraNodeInstallationHook {
    override fun hook(jira: BareJiraNode): RemoteResult {
        val ssh = jira.ssh
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
        return EmptyRemoteResult()
    }
}

class JdkInstall(
    private val jdk: JavaDevelopmentKit
) : JiraNodeInstallationHook {
    override fun hook(jira: BareJiraNode): RemoteResult {
        jdk.install(jira.ssh)
        return EmptyRemoteResult()
    }
}

class UbuntuSysstat : JiraNodeInstallationHook {
    override fun hook(jira: BareJiraNode): RemoteResult {
        val osMetrics = Ubuntu().metrics(jira.ssh)
        // when do we `osMetrics.map { it.start }` ??
        return TODO()
    }
}