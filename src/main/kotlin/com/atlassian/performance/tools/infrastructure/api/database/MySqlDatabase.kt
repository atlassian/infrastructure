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
        name = "mysql:5.7.32",
        pullTimeout = Duration.ofMinutes(5)
    )
    private val ubuntu = Ubuntu()

    /**
     * Arguments based on [jira docs](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-mysql-5-7-966063305.html).
     *
     * We skip setting `--sql-mode` even though the docs says:
     * "Ensure the sql_mode parameter does not specify NO_AUTO_VALUE_ON_ZERO".
     * It's unclear what value should be set and the based on [mysql docs](https://dev.mysql.com/doc/refman/5.7/en/sql-mode.html)
     * the defaults are good.
     */
    private val jiraDocsBasedArgs = listOf(
        "--default-storage-engine=INNODB",
        "--character-set-server=utf8mb4",
        "--innodb-default-row-format=DYNAMIC",
        "--innodb-large-prefix=ON",
        "--innodb-file-format=Barracuda",
        "--innodb-log-file-size=2G"
    ).joinToString(" ")

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
        image.run(
            ssh = ssh,
            parameters = "-p 3306:3306 -v `realpath $mysqlDataLocation`:/var/lib/mysql",
            arguments = "$jiraDocsBasedArgs --skip-grant-tables --max_connections=$maxConnections"
        )
        return mysqlDataLocation
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