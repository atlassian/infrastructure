package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test
import java.time.Duration
import java.util.function.Consumer

class DockerIT {
    @Test
    fun installWorks() {
        val privilegedContainer = SshUbuntuContainer(Consumer { it.setPrivilegedMode(true) })
        privilegedContainer.start().use { ssh ->
            ssh.toSsh().newConnection().use { connection ->
                //workaround for a bug in Docker download site for bionic
                val packageFile = "containerd.io_1.2.2-3_amd64.deb"
                connection.execute("curl -O https://download.docker.com/linux/ubuntu/dists/bionic/pool/edge/amd64/$packageFile", Duration.ofMinutes(3))
                connection.execute("sudo apt install ./$packageFile", Duration.ofMinutes(3))

                Docker().install(connection)
                DockerImage("hello-world").run(connection)
            }
        }
    }
}
