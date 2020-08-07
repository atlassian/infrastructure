package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.JiraInstance
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PostInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PostInstanceHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.network.TcpServerRoom
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.database.*
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection

class DockerMysqlServer private constructor(
    private val serverRoom: TcpServerRoom,
    private val mysqlVersion: String,
    private val source: DatasetPackage,
    private val maxConnections: Int,
    private val extraSqls: List<String>
) : PreInstanceHook {

    override fun call(
        nodes: List<PreInstallHooks>,
        hooks: PostInstanceHooks,
        reports: Reports
    ) {
        val server = serverRoom.serveTcp("mysql")
        val client = server.ssh.newConnection().use { setup(it, server) }
        nodes.forEach { node ->
            node.postInstall.insert(MysqlFiveDotSevenJiraConfig(server))
            node.postInstall.insert(MysqlFiveConnector())
        }
        hooks.insert(FixJiraUriViaMysql(client, server.ssh))
    }

    private fun setup(ssh: SshConnection, server: TcpNode): SshSqlClient {
        val mysqlDataLocation = source.download(ssh)
        val containerName = Mysql.container(
            dataDir = mysqlDataLocation,
            mysqlVersion = mysqlVersion,
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
        extraSqls.forEach { client.runSql(ssh, it) }
        return client
    }

    class Builder(
        private var serverRoom: TcpServerRoom,
        private var source: DatasetPackage
    ) {

        private var mysqlVersion: String = "5.7.32"
        private var maxConnections: Int = 151
        private var extraSqls: MutableList<String> = mutableListOf()

        fun serverRoom(serverRoom: TcpServerRoom) = apply { this.serverRoom = serverRoom }
        fun mysqlVersion(mysqlVersion: String) = apply { this.mysqlVersion = mysqlVersion }
        fun source(source: DatasetPackage) = apply { this.source = source }
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }
        fun setPassword(user: String, password: String) = apply {
            extraSqls.add("UPDATE jiradb.cwd_user SET credential='$password' WHERE user_name='$user';")
        }

        fun resetCaptcha(user: String) = apply {
            resetAttribute(user, "login.totalFailedCount")
            resetAttribute(user, "login.currentFailedCount")
        }

        private fun resetAttribute(user: String, attribute: String) {
            val sql = "UPDATE jiradb.cwd_user_attributes SET attribute_value = '0'" +
                "WHERE user_id = (SELECT id FROM jiradb.cwd_user WHERE user_name = '$user')" +
                "AND attribute_name = '$attribute';"
            extraSqls.add(sql)
        }

        fun build(): DockerMysqlServer = DockerMysqlServer(
            serverRoom,
            mysqlVersion,
            source,
            maxConnections,
            ArrayList(extraSqls)
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
