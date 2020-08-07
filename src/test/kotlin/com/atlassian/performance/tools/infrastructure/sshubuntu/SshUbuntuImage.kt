package com.atlassian.performance.tools.infrastructure.sshubuntu

import com.atlassian.performance.tools.infrastructure.docker.ConnectedContainer
import com.atlassian.performance.tools.infrastructure.docker.CreatedContainer
import com.atlassian.performance.tools.infrastructure.docker.StartedDockerContainer
import com.atlassian.performance.tools.infrastructure.docker.execAsResource
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshHost
import com.atlassian.performance.tools.ssh.api.auth.PasswordAuthentication
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import com.github.dockerjava.core.DockerClientBuilder
import java.time.Duration
import java.util.UUID

/**
 * Supports:
 * * [Ssh] to an Ubuntu with `sudo`
 * * Docker "next to" Docker - able to start Docker instances even if the host is not privileged
 * * GPG
 * * granular Docker resource allocation and deallocation
 */
class SshUbuntuImage(
    private val ubuntuVersion: String,
    private val docker: DockerClient,
    private val networkId: String,
    private val portsToExpose: List<Int>
) {

    fun <T> runInUbuntu(
        lambda: (SshUbuntuContainer) -> T
    ): T {
        docker
            .pullImageCmd("rastasheep/ubuntu-sshd")
            .withTag(ubuntuVersion)
            .exec(PullImageResultCallback())
            .awaitCompletion()
        val dockerDaemonSocket = "/var/run/docker.sock"
        return docker
            .createContainerCmd("rastasheep/ubuntu-sshd:$ubuntuVersion")
            .withHostConfig(
                HostConfig()
                    .withPublishAllPorts(true)
                    .withBinds(Bind(dockerDaemonSocket, Volume(dockerDaemonSocket)))
            )
            .withExposedPorts(
                portsToExpose.map { ExposedPort.tcp(it) }
            )
            .execAsResource(docker)
            .use { runInContainer(it, lambda) }
    }

    private fun <T> runInContainer(
        container: CreatedContainer,
        lambda: (SshUbuntuContainer) -> T
    ): T = docker
        .connectToNetworkCmd()
        .withContainerId(container.response.id)
        .withNetworkId(networkId)
        .execAsResource(docker)
        .use { runInConnectedContainer(it, lambda) }

    private fun <T> runInConnectedContainer(
        container: ConnectedContainer,
        lambda: (SshUbuntuContainer) -> T
    ): T = docker
        .startContainerCmd(container.containerId)
        .execAsResource(docker)
        .use { runInStartedContainer(it, lambda) }

    private fun <T> runInStartedContainer(
        container: StartedDockerContainer,
        lambda: (SshUbuntuContainer) -> T
    ): T {
        val networkSettings = docker
            .inspectContainerCmd(container.id)
            .exec()
            .networkSettings
        val ip = networkSettings.ipAddress
        val ports = networkSettings.ports
        val sshPort = ports
            .bindings[ExposedPort.tcp(22)]!!
            .single()
            .hostPortSpec
            .toInt()
        val sshHost = SshHost(
            ipAddress = "localhost",
            userName = "root",
            authentication = PasswordAuthentication("root"),
            port = sshPort
        )
        val ssh = Ssh(sshHost)
        ssh.newConnection().use {
            it.execute("apt-get update", Duration.ofMinutes(2))
            it.execute("apt-get -y install sudo gnupg screen")
        }
        return lambda(SshUbuntuContainer(ssh, ports, ip))
    }

    companion object {
        fun <T> runSoloUbuntu(ubuntuVersion: String, lambda: (SshUbuntuContainer) -> T): T {
            val docker = DockerClientBuilder.getInstance().build()
            return docker
                .createNetworkCmd()
                .withName(UUID.randomUUID().toString())
                .execAsResource(docker)
                .use { network ->
                    SshUbuntuImage(ubuntuVersion, docker, network.response.id, emptyList())
                        .runInUbuntu { lambda(it) }
                }
        }

        fun <T> runSoloUbuntu(lambda: (SshUbuntuContainer) -> T): T = runSoloUbuntu("18.04", lambda)

        fun <T> runSoloSsh(lambda: (SshConnection) -> T): T = runSoloSsh("18.04", lambda)

        fun <T> runSoloSsh(ubuntuVersion: String, lambda: (SshConnection) -> T): T {
            return runSoloUbuntu(ubuntuVersion) { ubuntu ->
                ubuntu.ssh.newConnection().use(lambda)
            }
        }
    }
}
