package com.atlassian.performance.tools.infrastructure.database

import com.atlassian.performance.tools.infrastructure.api.docker.DockerContainer
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.docker.DeadContainerCheck
import com.atlassian.performance.tools.jvmtasks.api.Backoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration
import java.time.Duration.ofSeconds

internal object Mysql {
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

    private val pollPeriod = Duration.ofMillis(500)
    private val maxWait = Duration.ofMinutes(15)

    fun installClient(ssh: SshConnection): SshSqlClient {
        ubuntu.install(ssh, listOf("mysql-client"))
        return SshMysqlClient()
    }

    fun container(
        dataDir: String,
        extraParameters: Array<String>,
        extraArguments: Array<String>,
        host: TcpNode? = null,
        mysqlVersion: String = "5.7.32"
    ) = DockerContainer.Builder()
        .imageName("mysql:$mysqlVersion")
        .pullTimeout(Duration.ofMinutes(5))
        .parameters(
            host?.port?.let { "-p $it:$it" } ?: "-p 3306:3306",
            "-v `realpath $dataDir`:/var/lib/mysql",
            *extraParameters
        )
        .arguments(
            *jiraDocsBasedArgs,
            *extraArguments
        )
        .build()

    fun awaitDatabase(ssh: SshConnection, sqlClient: SshSqlClient) {
        val backoff = StaticBackoff(pollPeriod)
        awaitDatabase(ssh, sqlClient, backoff)
    }

    fun awaitDatabase(ssh: SshConnection, sqlClient: SshSqlClient, containerName: String) {
        val backoff = DeadContainerCheck(containerName, ssh, StaticBackoff(pollPeriod))
        awaitDatabase(ssh, sqlClient, backoff)
    }

    private fun awaitDatabase(ssh: SshConnection, sqlClient: SshSqlClient, backoff: Backoff) {
        val maxAttempts = maxWait.toMillis() / pollPeriod.toMillis()
        IdempotentAction("wait for MySQL start") { sqlClient.runSql(ssh, "select 1;") }
            .retry(maxAttempts.toInt(), backoff)
    }
}