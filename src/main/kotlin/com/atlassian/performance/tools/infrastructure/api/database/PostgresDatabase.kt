package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.time.Duration

class PostgresDatabase(
    private val source: DatasetPackage,
    private val maxConnections: Int
) : Database {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    private val image: DockerImage = DockerImage(
        name = "postgres:9.6.15",
        pullTimeout = Duration.ofMinutes(5)
    )

    constructor(
        source: DatasetPackage
    ) : this(
        source = source,
        maxConnections = 200
    )

    override fun setup(ssh: SshConnection): String {
        val data = source.download(ssh)
        val containerName = image.run(
            ssh = ssh,
// TODO Dataset for Postgres
//            parameters = "-p 5432:5432 -v `realpath $data`:/",
            parameters = "-p 5432:5432",
            arguments = "-c 'listen_addresses='*'' -c 'max_connections=$maxConnections'"
        )
        Thread.sleep(Duration.ofSeconds(15).toMillis())
        logger.debug("Postgres - creating jira user and database")
        ssh.execute("sudo docker exec -u postgres $containerName psql --command \"CREATE USER jira WITH NOSUPERUSER INHERIT NOCREATEROLE NOCREATEDB LOGIN PASSWORD 'jira';\"")
        ssh.execute("sudo docker exec -u postgres $containerName createdb -E UNICODE -l C -T template0 -O jira jira")
        return data
    }

    override fun start(jira: URI, ssh: SshConnection) {
        // TODO Check logs for the following entry
        // LOG:  database system is ready to accept connections
        Thread.sleep(Duration.ofSeconds(15).toMillis())
    }

}