package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.docker.DockerImage
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.*
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class DockerMysqlServer private constructor(
    private val infrastructure: Infrastructure,
    private val source: DatasetPackage,
    private val dockerImage: DockerImage,
    private val maxConnections: Int
) : PreInstanceHook {

    override fun call(
        nodes: List<PreInstallHooks>,
        hooks: PreInstanceHooks,
        reports: Reports
    ) {
        val server = infrastructure.serve(3306, "mysql")
        val client = server.ssh.newConnection().use { setup(it, server) }
        nodes.forEach { node ->
            node.postInstall.insert(DatabaseIpConfig(server.ip))
            node.postInstall.insert(MysqlConnector())
        }
        hooks.postInstance.insert(FixJiraUriViaMysql(server.ssh, client))
    }

    private fun setup(ssh: SshConnection, host: TcpHost): SshSqlClient {
        val mysqlData = source.download(ssh)
        val port = host.port
        dockerImage.run(
            ssh = ssh,
            parameters = "-p $port:$port -v `realpath $mysqlData`:/var/lib/mysql",
            arguments = "--skip-grant-tables --max_connections=$maxConnections"
        )
        Ubuntu().install(ssh, listOf("mysql-client"))
        val client = SshMysqlClient("127.0.0.1", port, "root")
        IdempotentAction("wait for MySQL start") { client.runSql(ssh, "select 1;") }
            .retry(90, StaticBackoff(Duration.ofSeconds(10)))
        return client
    }

    class Builder(
        private var infrastructure: Infrastructure,
        private var source: DatasetPackage
    ) {

        private var dockerImage = DockerImage.Builder("mysql:5.6.42")
            .pullTimeout(Duration.ofMinutes(5))
            .build()
        private var maxConnections: Int = 151

        fun infrastructure(infrastructure: Infrastructure) = apply { this.infrastructure = infrastructure }
        fun source(source: DatasetPackage) = apply { this.source = source }
        fun dockerImage(dockerImage: DockerImage) = apply { this.dockerImage = dockerImage }
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build(): DockerMysqlServer = DockerMysqlServer(
            infrastructure,
            source,
            dockerImage,
            maxConnections
        )
    }

    private class FixJiraUriViaMysql(
        private val mysqlNode: Ssh,
        private val client: SshSqlClient
    ) : PostInstanceHook {

        override fun call(instance: JiraInstance, hooks: PostInstanceHooks, reports: Reports) {
            mysqlNode.newConnection().use { ssh ->
                val db = "jiradb"
                val update = "UPDATE $db.propertystring SET propertyvalue = '${instance.address}'"
                val where = "WHERE id IN (select id from $db.propertyentry where property_key like '%baseurl%')"
                client.runSql(ssh, "$update $where;")
            }
        }
    }
}
