package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.lib.docker.CreatedContainer
import com.atlassian.performance.tools.infrastructure.lib.docker.DockerNetwork
import com.atlassian.performance.tools.infrastructure.lib.docker.StartedContainer
import com.atlassian.performance.tools.infrastructure.lib.docker.execAsResource
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshHost
import com.atlassian.performance.tools.ssh.api.auth.PasswordAuthentication
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.NetworkSettings
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import java.time.Duration
import java.util.*
import java.util.UUID.randomUUID
import java.util.concurrent.ConcurrentLinkedDeque

internal class DockerInfrastructure(
    private val ubuntuVersion: String = "18.04"
) : Infrastructure {

    private val allocatedResources: Deque<AutoCloseable> = ConcurrentLinkedDeque()
    private val docker: DockerClient
    private val network: DockerNetwork


    init {
        val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
        val dockerHttp = ZerodepDockerHttpClient.Builder().dockerHost(dockerConfig.dockerHost).build()
        docker = DockerClientImpl.getInstance(dockerConfig, dockerHttp)
        allocatedResources.add(docker)
        network = docker
            .createNetworkCmd()
            .withName(randomUUID().toString())
            .execAsResource(docker)
        allocatedResources.add(network)
    }

    fun serveSsh(): Ssh = serve(888, "ssh").ssh

    override fun serve(port: Int, name: String): TcpHost {
        docker
            .pullImageCmd("rastasheep/ubuntu-sshd")
            .withTag(ubuntuVersion)
            .exec(PullImageResultCallback())
            .awaitCompletion()
        val exposedPort = ExposedPort.tcp(port)
        val createdContainer = docker
            .createContainerCmd("rastasheep/ubuntu-sshd:18.04")
            .withHostConfig(
                HostConfig()
                    .withPublishAllPorts(true)
                    .withPrivileged(true)
                    .withNetworkMode(network.response.id)
            )
            .withExposedPorts(exposedPort, ExposedPort.tcp(22))
            .withName("$name-${randomUUID()}")
            .execAsResource(docker)
        allocatedResources.addLast(createdContainer)
        return start(createdContainer, exposedPort, name)
    }

    private fun start(
        created: CreatedContainer,
        port: ExposedPort,
        name: String
    ): TcpHost {
        val startedContainer = docker
            .startContainerCmd(created.response.id)
            .execAsResource(docker)
        allocatedResources.addLast(startedContainer);
        return install(startedContainer, port, name)
    }

    private fun install(
        started: StartedContainer,
        port: ExposedPort,
        name: String
    ): TcpHost {
        val networkSettings = docker
            .inspectContainerCmd(started.id)
            .exec()
            .networkSettings
        val ip = networkSettings
            .networks
            .values
            .single { it.networkID == network.response.id }
            .ipAddress!!
        val sshPort = getHostPort(networkSettings, ExposedPort.tcp(22))
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
        return TcpHost("localhost", ip, port.port, name, ssh)
    }

    private fun getHostPort(
        networkSettings: NetworkSettings,
        port: ExposedPort
    ): Int {
        return networkSettings
            .ports
            .bindings[port]!!
            .single()
            .hostPortSpec
            .toInt()
    }

    override fun close() {
        while (true) {
            allocatedResources
                .pollLast()
                ?.use {}
                ?: break
        }
    }
}