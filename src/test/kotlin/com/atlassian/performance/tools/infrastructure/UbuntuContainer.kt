package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshHost
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.xfer.FileSystemFile
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.time.Duration

class UbuntuContainer {
    fun run(action: (ssh: SshConnection) -> Unit) {
        val sshPort = 22
        GenericContainerImpl("rastasheep/ubuntu-sshd:16.04")
            .withExposedPorts(sshPort)
            .waitingFor(Wait.forListeningPort()).use { ubuntuContainer ->
                ubuntuContainer.start()
                val mappedSshPort = ubuntuContainer.getMappedPort(sshPort)
                val privateKey = File(javaClass.getResource("ssh_key").toURI()).toPath()
                val ipAddress = ubuntuContainer.containerIpAddress
                copyAuthFile(ipAddress, mappedSshPort)
                val ssh = Ssh(
                    SshHost(
                        ipAddress = ipAddress,
                        userName = "root",
                        port = mappedSshPort,
                        key = privateKey
                    )
                )
                ssh.newConnection().use { sshConnection ->
                    sshConnection.execute("apt-get update -qq", Duration.ofMinutes(2))
                    sshConnection.execute("apt-get install sudo -y -qq")
                    action(sshConnection)
                }
            }
    }

    private fun copyAuthFile(ipAddress: String, port: Int) {
        SSHClient().use { sshClient ->
            sshClient.addHostKeyVerifier { _, _, _ -> true }
            sshClient.connect(ipAddress, port)
            sshClient.authPassword("root", "root")
            sshClient.newSCPFileTransfer().newSCPUploadClient().copy(
                FileSystemFile(javaClass.getResource("authorized_keys").path),
                "/root/.ssh/"
            )
        }
    }
}

/**
 * TestContainers depends on construction of recursive generic types like class C<SELF extends C<SELF>>. It doesn't work
 * in kotlin. See:
 * https://youtrack.jetbrains.com/issue/KT-17186
 * https://github.com/testcontainers/testcontainers-java/issues/318
 * The class is a workaround for the problem.
 */
private class GenericContainerImpl(dockerImageName: String) : GenericContainer<GenericContainerImpl>(dockerImageName)