package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.*
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.database.Mysql
import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection

class DockerMysqlServer private constructor(
    private val infrastructure: Infrastructure,
    private val source: DatasetPackage,
    private val maxConnections: Int
) : PreInstanceHook {

    override fun call(
        nodes: List<PreInstallHooks>,
        hooks: PreInstanceHooks,
        reports: Reports
    ) {
        val server = infrastructure.serveTcp("mysql")
        val client = server.ssh.newConnection().use { setup(it, server) }
        nodes.forEach { node ->
            node.postInstall.insert(DatabaseIpConfig(server.privateIp))
            node.postInstall.insert(MysqlConnector())
        }
        hooks.postInstance.insert(FixJiraUriViaMysql(client, server.ssh))
    }

    private fun setup(ssh: SshConnection, server: TcpNode): SshSqlClient {
        val mysqlDataLocation = source.download(ssh)
        val containerName = Mysql.container(
            dataDir = mysqlDataLocation,
            extraParameters = emptyArray(),
            extraArguments = arrayOf(
                "--skip-grant-tables", // Recovery mode, as some datasets give no permissions to their root DB user
                "--max_connections=$maxConnections"
            ),
            host = server
        ).run(ssh)
        Ubuntu().install(ssh, listOf("mysql-client"))
        val client = SshMysqlClient("127.0.0.1", server.port)
        Mysql.awaitDatabase(ssh, client, containerName)
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

        override fun call(instance: JiraInstance, hooks: PostInstanceHooks, reports: Reports) {
            ssh.newConnection().use { ssh ->
                val db = "jiradb"
                val update = "UPDATE $db.propertystring SET propertyvalue = '${instance.address}'"
                val where = "WHERE id IN (select id from $db.propertyentry where property_key like '%baseurl%')"
                client.runSql(ssh, "$update $where;")
            }
        }
    }
}
