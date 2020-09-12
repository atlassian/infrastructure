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
    private val maxConnections: Int,
    private val dataBaseVersion: String
) : Database {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    private val image: DockerImage = DockerImage(
        name = "postgres:$dataBaseVersion",
        pullTimeout = Duration.ofMinutes(5)
    )

    constructor(
        source: DatasetPackage,
        dataBaseVersion: String
    ) : this(
        source = source,
        maxConnections = 200,
        dataBaseVersion = dataBaseVersion
    )

    override fun setup(ssh: SshConnection): String {
        val pgData = source.download(ssh)
        image.run(
            ssh = ssh,
            parameters = "-p 3306:5432 -v `realpath $pgData`:/var/lib/postgresql/data",
            arguments = "-c 'listen_addresses='*'' -c 'max_connections=$maxConnections'"
        )
        return pgData
    }

    override fun start(jira: URI, ssh: SshConnection) {
        // TODO Check logs for the following entry
        // LOG:  database system is ready to accept connections
        Thread.sleep(Duration.ofSeconds(15).toMillis())
    }

    override fun type(): String = "postgres"
}