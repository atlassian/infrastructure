package com.atlassian.performance.tools.infrastructure.database

import com.atlassian.performance.tools.infrastructure.api.docker.DockerContainer
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Duration
import java.time.Instant

internal object Mysql {
    private val logger: Logger = LogManager.getLogger(this::class.java)
    private val ubuntu = Ubuntu()

    /**
     * Arguments based on [jira docs](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-mysql-5-7-966063305.html).
     *
     * We skip setting `--sql-mode` even though the docs says:
     * "Ensure the sql_mode parameter does not specify NO_AUTO_VALUE_ON_ZERO".
     * It's unclear what value should be set and the based on [mysql docs](https://dev.mysql.com/doc/refman/5.7/en/sql-mode.html)
     * the defaults are good.
     */
    private val jiraDocsBasedArgs = arrayOf(
        "--default-storage-engine=INNODB",
        "--character-set-server=utf8mb4",
        "--innodb-default-row-format=DYNAMIC",
        "--innodb-large-prefix=ON",
        "--innodb-file-format=Barracuda",
        "--innodb-log-file-size=2G"
    )

    fun installClient(ssh: SshConnection): SshSqlClient {
        ubuntu.install(ssh, listOf("mysql-client"))
        return SshMysqlClient()
    }

    fun container(
        dataDir: String,
        extraParameters: Array<String>,
        extraArguments: Array<String>,
        hostPort: Int = 3306
    ) = DockerContainer.Builder()
        .imageName("mysql:5.7.32")
        .pullTimeout(Duration.ofMinutes(5))
        .parameters(
            "-p $hostPort:3306",
            "-v `realpath $dataDir`:/var/lib/mysql",
            *extraParameters
        )
        .arguments(
            *jiraDocsBasedArgs,
            *extraArguments
        )
        .build()

    fun awaitDatabase(ssh: SshConnection) {
        val mysqlStart = Instant.now()
        while (!ssh.safeExecute("mysql -h 127.0.0.1 -u root -e 'select 1;'").isSuccessful()) {
            if (Instant.now() > mysqlStart + Duration.ofMinutes(15)) {
                throw RuntimeException("MySQL didn't start in time")
            }
            logger.debug("Waiting for MySQL...")
            Thread.sleep(Duration.ofSeconds(10).toMillis())
        }
    }
}