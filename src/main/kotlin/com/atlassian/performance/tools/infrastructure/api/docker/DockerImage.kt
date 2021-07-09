package com.atlassian.performance.tools.infrastructure.api.docker

import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.Level
import java.time.Duration
import java.util.*

class DockerImage private constructor(
    private val docker: Docker,
    private val name: String,
    private val pullTimeout: Duration
) {

    fun run(
        ssh: SshConnection
    ) = run(
        ssh = ssh,
        parameters = "",
        arguments = ""
    )

    fun run(
        ssh: SshConnection,
        parameters: String,
        arguments: String
    ): String {
        docker.install(ssh)
        val containerName = "jpt-" + UUID.randomUUID()
        ssh.execute(
            cmd = "sudo docker pull $name",
            timeout = pullTimeout,
            stdout = Level.TRACE,
            stderr = Level.WARN
        )
        ssh.execute("sudo docker run -d $parameters --name $containerName $name $arguments")
        return containerName
    }

    class Builder(
        private val name: String
    ) {

        private var docker = Docker.Builder().build()
        private var pullTimeout: Duration = Duration.ofMinutes(1)

        fun docker(docker: Docker) = apply { this.docker = docker }
        fun pullTimeout(pullTimeout: Duration) = apply { this.pullTimeout = pullTimeout }

        fun build(): DockerImage = DockerImage(
            docker,
            name,
            pullTimeout
        )
    }
}
