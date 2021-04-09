package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNode
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.lib.docker.*
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshHost
import com.atlassian.performance.tools.ssh.api.auth.PasswordAuthentication
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import java.time.Duration
import java.util.*
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
            .withName(UUID.randomUUID().toString())
            .execAsResource(docker)
        allocatedResources.add(network)
    }

    override fun serve(jiraNodePlans: List<JiraNodePlan>): List<JiraNode> {
        return jiraNodePlans.mapIndexed { nodeIndex, plan ->
            plan.materialize(serve(8080, "jira-node-$nodeIndex"))
        }
    }

    fun serveSsh(): Ssh = serve(888, "ssh").ssh

    override fun serve(port: Int, name: String): TcpHost {
        docker
            .pullImageCmd("rastasheep/ubuntu-sshd")
            .withTag(ubuntuVersion)
            .exec(PullImageResultCallback())
            .awaitCompletion()
        val exposedPort = ExposedPort.tcp(port)
        val dockerDaemonSocket = "/var/run/docker.sock"
        val createdContainer = docker
            .createContainerCmd("rastasheep/ubuntu-sshd:18.04")
            .withHostConfig(
                HostConfig()
                    .withPublishAllPorts(true)
//                    .withPrivileged(true)
                    .withBinds(Bind(dockerDaemonSocket, Volume(dockerDaemonSocket)))
            )
            .withExposedPorts(exposedPort)
            .withName(name + "-" + UUID.randomUUID())
            .execAsResource(docker)
        allocatedResources.addLast(createdContainer)
        return start(createdContainer, exposedPort)
    }

    private fun start(
        created: CreatedContainer,
        port: ExposedPort
    ): TcpHost {
        val connectedContainer = docker
            .connectToNetworkCmd()
            .withContainerId(created.response.id)
            .withNetworkId(network.response.id)
            .execAsResource(docker)
        allocatedResources.addLast(connectedContainer)
        return start(connectedContainer, port)
    }

    private fun start(
        connected: ConnectedContainer,
        port: ExposedPort
    ): TcpHost {
        val startedContainer = docker
            .startContainerCmd(connected.containerId)
            .execAsResource(docker)
        allocatedResources.addLast(startedContainer);
        return install(startedContainer, connected, port)
    }

    private fun install(
        started: StartedContainer,
        connected: ConnectedContainer,
        port: ExposedPort
    ): TcpHost {
        val networkSettings = docker
            .inspectContainerCmd(started.id)
            .exec()
            .networkSettings
        val ip = networkSettings
            .networks
            .values
            .single { it.networkID == connected.networkId }
            .ipAddress!!
        val portBindings = networkSettings.ports.bindings
        val sshPort = getHostPort(portBindings, ExposedPort.tcp(22))
        val tcpPort = getHostPort(portBindings, port)
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
        return TcpHost("localhost", ip, tcpPort, started.id, ssh)
    }

    private fun getHostPort(
        portBindings: MutableMap<ExposedPort, Array<Ports.Binding>>,
        port: ExposedPort
    ): Int {
        return portBindings[port]!!
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