package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.time.Duration
import java.time.Instant

/**
 * Since 4.21.0, it's compatible with Ubuntu 16.04, 18.04, 20.04 and 22.04.
 * @param maxConnections MySQL `max_connections` parameter.
 */
class MySqlDatabase(
    private val source: DatasetPackage,
    private val maxConnections: Int
) : Database {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    private val image: DockerImage = DockerImage(
        name = "mysql/mysql-server:5.7.32",
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
        val mysqlDataLocation = source.download(ssh)
        val bindPorts = "-p 3306:3306"
        val mountDataset = "-v `realpath $mysqlDataLocation`:/var/lib/mysql"
        val ignorePassword = "--env MYSQL_ALLOW_EMPTY_PASSWORD=yep"
        val container = image.run(
            ssh = ssh,
            parameters = "$bindPorts $mountDataset $ignorePassword",
            arguments = "--max_connections=$maxConnections"
        )
        upgrade(ssh, container)
        return mysqlDataLocation
    }

    /**
     * See [MySQL in Docker docs](https://dev.mysql.com/doc/refman/5.7/en/docker-mysql-getting-started.html).
     */
    private fun upgrade(ssh: SshConnection, container: String) {
        // annoyingly, the script exits with 2 when it's already upgraded, hence `safeExecute`
        val upgrade = ssh.safeExecute("sudo docker exec $container mysql_upgrade -uroot")
        if (upgrade.isSuccessful()) {
            ssh.execute("sudo docker restart $container")
        }
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
}