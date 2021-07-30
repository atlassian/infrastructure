package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.*
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.network.TcpServerRoom
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.database.Mysql
import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection

class DockerMysqlServer private constructor(
    private val serverRoom: TcpServerRoom,
    private val source: DatasetPackage,
    private val maxConnections: Int
) : PreInstanceHook {

    override fun call(
        nodes: List<PreInstallHooks>,
        hooks: PostInstanceHooks,
        reports: Reports
    ) {
        val server = serverRoom.serveTcp("mysql")
        val client = server.ssh.newConnection().use { setup(it, server) }
        nodes.forEach { node ->
            node.postInstall.insert(DatabaseIpConfig(server.privateIp))
            node.postInstall.insert(MysqlConnector())
        }
        hooks.insert(FixJiraUriViaMysql(client, server.ssh))
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
        private var serverRoom: TcpServerRoom,
        private var source: DatasetPackage
    ) {

        private var maxConnections: Int = 151

        fun serverRoom(serverRoom: TcpServerRoom) = apply { this.serverRoom = serverRoom }
        fun source(source: DatasetPackage) = apply { this.source = source }
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build(): DockerMysqlServer = DockerMysqlServer(
            serverRoom,
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
