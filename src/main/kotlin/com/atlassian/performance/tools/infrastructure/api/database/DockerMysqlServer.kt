package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PostInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PostInstanceHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHooks
import com.atlassian.performance.tools.infrastructure.database.Mysql
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class DockerMysqlServer private constructor(
    private val infrastructure: Infrastructure,
    private val source: DatasetPackage,
    private val maxConnections: Int
) : PreInstanceHook {

    override fun call(hooks: PreInstanceHooks) {
        val server = infrastructure.serve(3306, "mysql")
        val client = server.ssh.newConnection().use { setup(it, server) }
        hooks.nodes.forEach { node ->
            node.postInstall.insert(DatabaseIpConfig(server.ip))
            node.postInstall.insert(MysqlConnector())
        }
        hooks.postInstance.insert(FixJiraUriViaMysql(client, server.ssh))
    }

    private fun setup(ssh: SshConnection, server: TcpHost): SshSqlClient {
        val mysqlDataLocation = source.download(ssh)
        Mysql.container(
            dataDir = mysqlDataLocation,
            extraParameters = emptyArray(),
            extraArguments = arrayOf(
                "--skip-grant-tables", // Recovery mode, as some datasets give no permissions to their root DB user
                "--max_connections=$maxConnections"
            ),
            host = server
        ).run(ssh)
        val client = Mysql.installClient(ssh)
        Mysql.awaitDatabase(ssh)
        return client
    }

    class Builder(
        private var infrastructure: Infrastructure,
        private var source: DatasetPackage
    ) {

        private var maxConnections: Int = 151

        fun infrastructure(infrastructure: Infrastructure) = apply { this.infrastructure = infrastructure }
        fun source(source: DatasetPackage) = apply { this.source = source }
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build(): DockerMysqlServer = DockerMysqlServer(
            infrastructure,
            source,
            maxConnections
        )
    }

    private class FixJiraUriViaMysql(
        private val client: SshSqlClient,
        private val ssh: Ssh
    ) : PostInstanceHook {

        override fun call(instance: URI, hooks: PostInstanceHooks) {
            ssh.newConnection().use { ssh ->
                val db = "jiradb"
                val update = "UPDATE $db.propertystring SET propertyvalue = '$instance'"
                val where = "WHERE id IN (select id from $db.propertyentry where property_key like '%baseurl%')"
                client.runSql(ssh, "$update $where;")
            }
        }
    }
}
