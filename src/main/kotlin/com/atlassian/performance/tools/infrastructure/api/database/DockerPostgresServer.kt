package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.docker.DockerImage
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHooks
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Duration
import java.util.function.Supplier

class DockerPostgresServer private constructor(
    private val hostSupplier: Supplier<TcpHost>,
    private val dockerImage: DockerImage,
    private val maxConnections: Int
) : PreInstanceHook {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun call(hooks: PreInstanceHooks) {
        val server = hostSupplier.get()
        server.ssh.newConnection().use { setup(it) }
        hooks.nodes.forEach { node ->
            node.postInstall.insert(DatabaseIpConfig(server.ip))
        }
    }

    private fun setup(ssh: SshConnection) {
        val container = dockerImage.run(
            ssh = ssh,
            // TODO Dataset for Postgres
            // parameters = "-p 5432:5432 -v `realpath $data`:/",
            parameters = "-p 5432:5432",
            arguments = "-c 'listen_addresses='*'' -c 'max_connections=$maxConnections'"
        )
        Thread.sleep(Duration.ofSeconds(15).toMillis())
        logger.debug("Postgres - creating jira user and database")
        ssh.execute("sudo docker exec -u postgres $container psql --command \"CREATE USER jira WITH NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN PASSWORD 'jira';\"")
        ssh.execute("sudo docker exec -u postgres $container createdb -E UNICODE -l C -T template0 -O jira jira")
        /**
         * TODO Check logs for the following entry
         * `LOG:  database system is ready to accept connections`
         */
        Thread.sleep(Duration.ofSeconds(15).toMillis())
    }

    class Builder(
        private var hostSupplier: Supplier<TcpHost>
    ) {

        private var dockerImage = DockerImage.Builder("postgres:9.6.15")
            .pullTimeout(Duration.ofMinutes(5))
            .build()
        private var maxConnections: Int = 200

        fun serverSupplier(hostSupplier: Supplier<TcpHost>) = apply { this.hostSupplier = hostSupplier }
        fun dockerImage(dockerImage: DockerImage) = apply { this.dockerImage = dockerImage }
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build(): DockerPostgresServer = DockerPostgresServer(
            hostSupplier,
            dockerImage,
            maxConnections
        )
    }
}
