package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.docker.DockerContainer
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
        DockerInfrastructure(version).use { infra ->
            infra.serveSsh().newConnection().use { ssh ->
                DockerContainer.Builder()
                    .imageName("hello-world")
                    .build()
                    .run(ssh)
            }
        }
    }

    @Test
    fun shouldInstallIdempotently() {
        DockerInfrastructure().use { infra ->
            infra.serveSsh().newConnection().use { ssh ->
                Docker().install(ssh)
                Docker().install(ssh)
            }
        }
    }
}
