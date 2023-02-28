package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.database.Mysql
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class MinimalMysqlDatabase private constructor(
    private val jiraDbUserPassword: String,
    private val maxConnections: Int
) : Database {
    override fun setup(ssh: SshConnection): String {
        val mysqlDataLocation = "mySqlData"
        ssh.execute("mkdir -p $mysqlDataLocation")
        Mysql
            .container(
                dataDir = mysqlDataLocation,
                extraParameters = arrayOf("-e MYSQL_ALLOW_EMPTY_PASSWORD=yes"),
                extraArguments = arrayOf("--max_connections=$maxConnections")
            )
            .run(ssh)
        return mysqlDataLocation
    }

    override fun start(jira: URI, ssh: SshConnection) {
        val client = Mysql.installClient(ssh)
        Mysql.awaitDatabase(ssh)

        // Based on [jira docs](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-mysql-5-7-966063305.html)
        client.runSql(ssh, "CREATE USER 'jiradbuser'@'%' IDENTIFIED BY '$jiraDbUserPassword';")
        client.runSql(ssh, "CREATE DATABASE jiradb CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;")
        client.runSql(ssh, """
            GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP,REFERENCES,ALTER,INDEX on jiradb.* TO 'jiradbuser'@'%' IDENTIFIED BY '$jiraDbUserPassword';
            flush privileges;
        """.trimIndent())
    }

    class Builder {
        private var jiraDbUserPassword: String = "password"

        private var maxConnections: Int = 151

        fun jiraDbUserPassword(jiraDbUserPassword: String) = apply { this.jiraDbUserPassword = jiraDbUserPassword }

        @Suppress("unused")
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build() = MinimalMysqlDatabase(
            jiraDbUserPassword,
            maxConnections
        )
    }
}