package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.docker.DockerContainer
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHooks
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds
import java.util.function.Supplier

class DockerPostgresServer private constructor(
    private val serverSupplier: Supplier<TcpServer>,
    private val source: DatasetPackage,
    private val maxConnections: Int
) : PreInstanceHook {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun call(hooks: PreInstanceHooks) {
        val server = serverSupplier.get()
        server.ssh.newConnection().use { setup(it) }
        hooks.nodes.forEach { node ->
            node.postInstall.insert(DatabaseIpConfig(server.ip))
        }
    }

    private fun setup(ssh: SshConnection) {
        val data = source.download(ssh)
        val containerName = DockerContainer.Builder()
            .imageName("postgres:9.6.15")
            .pullTimeout(ofMinutes(5))
            .parameters("-p 5432:5432", "-v `realpath $data`:/${TODO("download and mount Postgres data")}")
            .arguments("-c 'listen_addresses='*''", "-c 'max_connections=$maxConnections'")
            .build()
            .run(ssh)
        Thread.sleep(ofSeconds(15).toMillis())
        ssh.execute("sudo docker exec -u postgres $containerName psql --command \"CREATE USER jira WITH NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN PASSWORD 'jira';\"")
        ssh.execute("sudo docker exec -u postgres $containerName createdb -E UNICODE -l C -T template0 -O jira jira")
        /**
         * TODO Check logs for the following entry
         * `LOG:  database system is ready to accept connections`
         */
        Thread.sleep(ofSeconds(15).toMillis())
    }

    class Builder(
        private var serverSupplier: Supplier<TcpServer>,
        private var source: DatasetPackage
    ) {
        private var maxConnections: Int = 200

        fun serverSupplier(serverSupplier: Supplier<TcpServer>) = apply { this.serverSupplier = serverSupplier }
        fun source(source: DatasetPackage) = apply { this.source = source }
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build(): DockerPostgresServer = DockerPostgresServer(
            serverSupplier,
            source,
            maxConnections
        )
    }
}
