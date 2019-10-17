package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.docker.DockerImage
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshHost
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Duration
import java.time.Instant

class DockerMysqlServer private constructor(
    private val source: DatasetPackage,
    private val dockerImage: DockerImage,
    private val maxConnections: Int
) {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    fun setup(sshHost: SshHost) {
        Ssh(host = sshHost, connectivityPatience = 4)
            .newConnection()
            .use { setup(it) }
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
        private val source: DatasetPackage
    ) {

        private var dockerImage = DockerImage.Builder("mysql:5.6.42")
            .pullTimeout(Duration.ofMinutes(5))
            .build()
        private var maxConnections: Int = 151

        fun dockerImage(dockerImage: DockerImage) = apply { this.dockerImage = dockerImage }
        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build(): DockerMysqlServer = DockerMysqlServer(
            source,
            dockerImage,
            maxConnections
        )
    }
}
