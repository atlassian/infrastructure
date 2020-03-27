package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test

class DockerIT {

    @Test
    fun shouldStartDocker() {
        SshUbuntuContainer().start().use { ssh ->
            ssh.toSsh().newConnection().use { connection ->
                Docker().install(connection)
                connection.execute("docker ps")
            }
        }
    }
}