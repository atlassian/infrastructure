package com.atlassian.performance.tools.infrastructure.api.docker

import com.atlassian.performance.tools.infrastructure.Docker
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.Level
import java.time.Duration
import java.util.*

class DockerContainer private constructor(
    private val runtime: Docker,
    private val imageName: String,
    private val pullTimeout: Duration,
    private val containerName: String,
    private val parameters: String,
    private val arguments: String
) {

    /**
     * @return container name
     */
    fun run(ssh: SshConnection): String {
        runtime.install(ssh)
        ssh.execute(
            cmd = "sudo docker pull $imageName",
            timeout = pullTimeout,
            stdout = Level.TRACE,
            stderr = Level.WARN
        )
        ssh.execute("sudo docker run -d $parameters --name $containerName $imageName $arguments")
        return containerName
    }

    class Builder {
        private val runtime: Docker = Docker()
        private var imageName: String = "ubuntu:latest"
        private var pullTimeout: Duration = Duration.ofMinutes(1)
        private var containerName: String = "jpt-" + UUID.randomUUID()
        private var parameters: String = ""
        private var arguments: String = ""

        fun imageName(imageName: String) = apply { this.imageName = imageName }
        fun pullTimeout(pullTimeout: Duration) = apply { this.pullTimeout = pullTimeout }
        fun containerName(containerName: String) = apply { this.containerName = containerName }
        fun parameters(parameters: String) = apply { this.parameters = parameters }
        fun arguments(arguments: String) = apply { this.arguments = arguments }

        fun build(): DockerContainer = DockerContainer(
            runtime,
            imageName,
            pullTimeout,
            containerName,
            parameters,
            arguments
        )
    }
}
