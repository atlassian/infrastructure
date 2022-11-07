package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test

class DockerIT {
    @Test
    fun shouldInstallOnXenial() {
        testDockerInstall("16.04")
    }

    @Test
    fun shouldInstallOnBionic() {
        testDockerInstall("18.04")
    }

    @Test
    fun shouldInstallOnFocal() {
        testDockerInstall("20.04")
    }

    @Test
    fun shouldInstallOnJammy() {
        testDockerInstall("22.04")
    }

    private fun testDockerInstall(version: String) {
        SshUbuntuContainer.Builder()
            .enableDocker()
            .version(version)
            .build()
            .start()
            .use { ubuntu ->
                ubuntu.toSsh().newConnection().use { connection ->
                    DockerImage("hello-world").run(connection)
                }
            }
    }
}
