package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.SshConnection
import org.apache.logging.log4j.Level
import java.time.Duration
import java.util.*

internal class DockerImage(
    private val name: String,
    private val pullTimeout: Duration = Duration.ofMinutes(1)
) {

    private val docker = Docker()

    fun run(
        ssh: SshConnection,
        parameters: String = "",
        arguments: String = ""
    ): String {
        docker.install(ssh)
        val containerName = "jpt-" + UUID.randomUUID()
        ssh.execute(
            cmd = "sudo docker pull $name",
            timeout = pullTimeout,
            stdout = Level.TRACE
        )
        ssh.execute("sudo docker run -d $parameters --name $containerName $name $arguments")
        return containerName
    }
}