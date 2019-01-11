package com.atlassian.performance.tools.infrastructure.docker

import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshHost
import com.atlassian.performance.tools.ssh.api.auth.PublicKeyAuthentication
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder
import net.schmizz.sshj.connection.channel.forwarded.SocketForwardingConnectListener
import net.schmizz.sshj.xfer.FileSystemFile
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.net.InetSocketAddress
import java.time.Duration

/**
 * Creates ubuntu container with open SSH port.
 *
 * @param publishing Container's internal ports to publish (expose) to the host.
 * See [docker documentation](https://docs.docker.com/engine/reference/commandline/run#publish-or-expose-port--p---expose).
 * @param forwarding Creates an ssh tunnel to expose host's port as a local container port.
 * Useful if you'd like to access local services from a docker's container.
 *
 * @return Port publications (mappings between container's exposed port and host port).
 *
 */
internal class SshUbuntuContainer(
    private val publishing: List<Int> = emptyList(),
    private val forwarding: List<ForwardedPort> = emptyList()
) {
    internal companion object {
        internal const val SSH_PORT = 22
        internal const val SSH_USERNAME = "root"
        internal const val SSH_PASSWORD = "root"
    }

    internal fun run(
        action: (ssh: SshConnection) -> Unit
    ) {
        run { ssh, _ -> action(ssh) }
    }

    internal fun run(
        action: (ssh: SshConnection, portPublications: PortPublications) -> Unit
    ) {
        val allExposedPorts = publishing + SSH_PORT
        GenericContainerImpl("rastasheep/ubuntu-sshd:18.04")
            .withExposedPorts(*allExposedPorts.toTypedArray())
            .waitingFor(Wait.forListeningPort())
            .use { ubuntuContainer ->
                ubuntuContainer.start()
                val sshPort = getHostSshPort(ubuntuContainer)
                val privateKey = File(javaClass.getResource("ssh_key").toURI()).toPath()
                val ipAddress = ubuntuContainer.containerIpAddress
                val ssh = Ssh(
                    SshHost(
                        ipAddress = ipAddress,
                        userName = SSH_USERNAME,
                        port = sshPort,
                        authentication = PublicKeyAuthentication(privateKey)
                    )
                )
                copyAuthFile(ipAddress, sshPort)
                provision(ssh)

                val portBindings = PortPublications(
                    ipAddress,
                    publishing.map { internalPort ->
                        val externalPort = ubuntuContainer.getMappedPort(internalPort)
                        PublishedPort(internalPort, externalPort)
                    }.toList()
                )

                ssh.newConnection().use { sshConnection ->
                    SshPortForwarder(
                        ipAddress,
                        sshPort,
                        SSH_USERNAME,
                        SSH_PASSWORD
                    ).forward(forwarding).use {
                        action(sshConnection, portBindings)
                    }
                }
            }
    }

    private fun getHostSshPort(ubuntuContainer: GenericContainerImpl) =
        ubuntuContainer.getMappedPort(SSH_PORT)

    private fun provision(ssh: Ssh) {
        ssh.newConnection().use { sshConnection ->
            sshConnection.execute("apt-get update -qq", Duration.ofMinutes(3))
            sshConnection.execute("apt-get install sudo screen gnupg2 -y -qq")
        }
    }

    /**
     * Docker container's port exposed as a host port. See
     * [Publish or expose port](https://docs.docker.com/engine/reference/commandline/run#publish-or-expose-port--p---expose)
     */
    internal class PublishedPort(val dockerPort: Int, val hostPort: Int)

    internal class PortPublications(
        internal val ipAddress: String,
        private val ports: List<PublishedPort>
    ) {
        internal fun getHostPort(dockerPort: Int): Int {
            return ports
                .single { it.dockerPort == dockerPort }
                .hostPort
        }
    }

    private fun copyAuthFile(ipAddress: String, port: Int) {
        SSHClient().use { sshClient ->
            sshClient.addHostKeyVerifier { _, _, _ -> true }
            sshClient.connect(ipAddress, port)
            sshClient.authPassword(SSH_USERNAME, SSH_PASSWORD)
            sshClient.newSCPFileTransfer().newSCPUploadClient().copy(
                FileSystemFile(javaClass.getResource("authorized_keys").path),
                "/root/.ssh/"
            )
        }
    }
}


internal class ForwardedPort(val remotePort: Int, val localPort: Int)

private class SshPortForwarder(
    private val ipAddress: String,
    private val sshPort: Int,
    private val username: String,
    private val password: String
) {

    internal fun forward(ports: List<ForwardedPort>): AutoCloseable {
        val sshClient = SSHClient()
        val sshClients: List<RemotePortForwarder.Forward> = ports.map { forwardHostPort(sshClient, it) }
        return AutoCloseable {
            sshClients.forEach { forward ->
                sshClient.remotePortForwarder.cancel(forward)
            }
            sshClient.disconnect()
        }
    }

    private fun forwardHostPort(sshClient: SSHClient, port: ForwardedPort): RemotePortForwarder.Forward {
        sshClient.addHostKeyVerifier { _, _, _ -> true }
        sshClient.connect(ipAddress, sshPort)
        sshClient.authPassword(username, password)
        return sshClient.remotePortForwarder.bind(
            RemotePortForwarder.Forward(port.remotePort),
            SocketForwardingConnectListener(InetSocketAddress("localhost", port.localPort)))
    }
}
/**
 * TestContainers depends on construction of recursive generic types like class C<SELF extends C<SELF>>. It doesn't work
 * in kotlin. See:
 * https://youtrack.jetbrains.com/issue/KT-17186
 * https://github.com/testcontainers/testcontainers-java/issues/318
 * The class is a workaround for the problem.
 */
private class GenericContainerImpl(dockerImageName: String) : GenericContainer<GenericContainerImpl>(dockerImageName) {
    override fun getLivenessCheckPortNumbers(): Set<Int> {
        return setOf(getMappedPort(SshUbuntuContainer.SSH_PORT))
    }
}