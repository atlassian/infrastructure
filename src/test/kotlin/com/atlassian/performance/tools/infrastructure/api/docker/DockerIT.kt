package com.atlassian.performance.tools.infrastructure.api.docker

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Volume
import org.junit.Test
import org.testcontainers.containers.GenericContainer
import java.time.Duration
import java.util.function.Consumer

class DockerIT {
    @Test
    fun installWorks() {
        SshUbuntuContainer(Consumer { enableNestedDocker(it) }).start().use { ssh ->
            ssh.toSsh().newConnection().use { connection ->
                //workaround for a bug in Docker download site for bionic
                val packageFile = "containerd.io_1.2.2-3_amd64.deb"
                connection.execute("curl -O https://download.docker.com/linux/ubuntu/dists/bionic/pool/edge/amd64/$packageFile", Duration.ofMinutes(3))
                connection.execute("sudo apt install ./$packageFile", Duration.ofMinutes(3))

                Docker.Builder().build().install(connection)
                DockerImage.Builder("hello-world").build().run(connection)
            }
        }
    }

    private fun enableNestedDocker(container: GenericContainer<*>) {
        val dockerDaemonSocket = "/var/run/docker.sock"
        container.setBinds(listOf(Bind(dockerDaemonSocket, Volume(dockerDaemonSocket))))
    }
}
