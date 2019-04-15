package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHookSequence
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.jvmtasks.api.Backoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.time.Duration
import java.time.Instant

/**
 * @param maxConnections MySQL `max_connections` parameter.
 */
class MySqlDatabase(
    private val source: DatasetPackage,
    private val maxConnections: Int
) : InstallableDatabase {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    private val image: DockerImage = DockerImage(
        name = "mysql:5.7.32",
        pullTimeout = Duration.ofMinutes(5)
    )
    private val ubuntu = Ubuntu()

    /**
     * Uses MySQL defaults.
     */
    constructor(
        source: DatasetPackage
    ) : this(
        source = source,
        maxConnections = 151
    )

    override fun setup(ssh: SshConnection): String {
        val mysqlData = source.download(ssh)
        image.run(
            ssh = ssh,
            parameters = "-p 3306:3306 -v `realpath $mysqlData`:/var/lib/mysql",
            arguments = "--skip-grant-tables --max_connections=$maxConnections"
        )
        return mysqlData
    }

    override fun start(jira: URI, ssh: SshConnection) {
        waitForMysql(ssh)
        ssh.execute("""mysql -h 127.0.0.1  -u root -e "UPDATE jiradb.propertystring SET propertyvalue = '$jira' WHERE id IN (select id from jiradb.propertyentry where property_key like '%baseurl%');" """)
    }

    private fun waitForMysql(ssh: SshConnection) {
        ubuntu.install(ssh, listOf("mysql-client"))
        val mysqlStart = Instant.now()
        while (!ssh.safeExecute("mysql -h 127.0.0.1 -u root -e 'select 1;'").isSuccessful()) {
            if (Instant.now() > mysqlStart + Duration.ofMinutes(15)) {
                throw RuntimeException("MySql didn't start in time")
            }
            logger.debug("Waiting for MySQL...")
            Thread.sleep(Duration.ofSeconds(10).toMillis())
        }
    }

    override fun installInJira(databaseIp: String): InstalledJiraHook {
        return InstalledJiraHookSequence(listOf(
            MysqlJdbc(databaseIp),
            MysqlConnector()
        ))
    }
}

private class MysqlConnector : InstalledJiraHook {

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

private class MysqlJdbc(
    private val databaseIp: String
) : InstalledJiraHook {

    override fun hook(
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

private class StaticBackoff(
    private val backOff: Duration
) : Backoff {
    override fun backOff(attempt: Int): Duration = backOff
}
