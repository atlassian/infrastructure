package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.docker.DockerImage
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PostInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PostInstanceHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHooks
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.function.Supplier

class DockerMysqlServer private constructor(
    private val serverSupplier: Supplier<TcpServer>,
    private val source: DatasetPackage,
    private val dockerImage: DockerImage,
    private val maxConnections: Int
) : PreInstanceHook {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun call(hooks: PreInstanceHooks) {
        val server = serverSupplier.get()
        server.ssh.newConnection().use { setup(it) }
        hooks.nodes.forEach { node ->
            node.postInstall.insert(DatabaseIpConfig(server.ip))
            node.postInstall.insert(MysqlConnector())
        }
        hooks.postInstance.insert(FixJiraUriViaMysql(server.ssh))
    }

    private fun setup(ssh: SshConnection) {
        val mysqlData = source.download(ssh)
        dockerImage.run(
            ssh = ssh,
            parameters = "-p 3306:3306 -v `realpath $mysqlData`:/var/lib/mysql",
            arguments = "--skip-grant-tables --max_connections=$maxConnections"
        )
        Ubuntu().install(ssh, listOf("mysql-client"))
        val deadline = Instant.now() + Duration.ofMinutes(15)
        while (ssh.safeExecute("mysql -h 127.0.0.1 -u root -e 'select 1;'").isSuccessful().not()) {
            if (Instant.now() > deadline) {
                throw Exception("MySQL didn't start in time")
            }
            logger.debug("Waiting for MySQL...")
            Thread.sleep(Duration.ofSeconds(10).toMillis())
        }
    }

    class Builder(
        private var serverSupplier: Supplier<TcpServer>,
        private var source: DatasetPackage
    ) {

        private var dockerImage = DockerImage.Builder("mysql:5.6.42")
            .pullTimeout(Duration.ofMinutes(5))
            .build()
        private var maxConnections: Int = 151

        fun serverSupplier(serverSupplier: Supplier<TcpServer>) = apply { this.serverSupplier = serverSupplier }
        fun source(source: DatasetPackage) = apply { this.source = source }
        fun dockerImage(dockerImage: DockerImage) = apply { this.dockerImage = dockerImage }
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build(): DockerMysqlServer = DockerMysqlServer(
            serverSupplier,
            source,
            dockerImage,
            maxConnections
        )
    }

    private class FixJiraUriViaMysql(
        private val mysql: Ssh
    ) : PostInstanceHook {

        override fun call(instance: URI, hooks: PostInstanceHooks) {
            mysql.newConnection().use { ssh ->
                val db = "jiradb"
                val update = "UPDATE $db.propertystring SET propertyvalue = '$instance'"
                val where = "WHERE id IN (select id from $db.propertyentry where property_key like '%baseurl%')"
                SshMysqlClient().runSql(ssh, "$update $where;")
            }
        }
    }
}
