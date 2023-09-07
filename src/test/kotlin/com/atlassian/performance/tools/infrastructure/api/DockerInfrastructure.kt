package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.api.network.HttpServerRoom
import com.atlassian.performance.tools.infrastructure.api.network.Networked
import com.atlassian.performance.tools.infrastructure.api.network.SshServerRoom
import com.atlassian.performance.tools.infrastructure.api.network.TcpServerRoom
import com.atlassian.performance.tools.infrastructure.lib.docker.CreatedContainer
import com.atlassian.performance.tools.infrastructure.lib.docker.DockerNetwork
import com.atlassian.performance.tools.infrastructure.lib.docker.StartedContainer
import com.atlassian.performance.tools.infrastructure.lib.docker.execAsResource
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
import java.util.UUID.randomUUID
import java.util.concurrent.ConcurrentLinkedDeque

internal class DockerInfrastructure(
    private val ubuntuVersion: String = "18.04"
) : SshServerRoom, TcpServerRoom, HttpServerRoom, Networked, AutoCloseable {

    private val allocatedResources: Deque<AutoCloseable> = ConcurrentLinkedDeque()
    private val docker: DockerClient
    private val network: DockerNetwork
    private val subnetCidr: String

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
        subnetCidr = docker
            .inspectNetworkCmd()
            .withNetworkId(network.response.id)
            .exec()
            .ipam
            .config
            .first()
            .subnet
    }

    override fun subnet(): String = subnetCidr

    fun serveSsh(): Ssh = serveSsh("ssh")

    override fun serveSsh(name: String): Ssh {
        return serveTcp(888, name).ssh
    }

    override fun serveTcp(name: String): TcpNode {
        // TODO pre-provision all the hosts rather than on-demand - unlock batch provisioning (CFN Stack), picking EC2 types, SSD storage, TCP port ranges, subnets, etc.
        return when {
            name.startsWith("jira-node") -> serveTcp(8080, name) // TODO this is a contract on undocumented behavior
            name.startsWith("mysql") -> serveTcp(3306, name)
            name.startsWith("samba") -> serveTcp(3306, name)
            else -> serveTcp(888, name)
        }
    }

    override fun serveHttp(name: String): HttpNode {
        return HttpNode(
            serveTcp(80, name),
            "/",
            false
        )
    }

    private fun serveTcp(tcpPort: Int, name: String): TcpNode {
        return serveTcp(name, listOf(tcpPort), emptyList())
    }


    override fun serveTcp(name: String, tcpPorts: List<Int>, udpPorts: List<Int>): TcpNode {
        val ports = tcpPorts.map { ExposedPort.tcp(it) } +
            udpPorts.map { ExposedPort.udp(it) } +
            ExposedPort.tcp(22)
        val imageName = "takeyamajp/ubuntu-sshd"
        val imageTag = "ubuntu$ubuntuVersion"
        docker
            .pullImageCmd(imageName)
            .withTag(imageTag)
            .exec(PullImageResultCallback())
            .awaitCompletion()
        val createdContainer = docker
            .createContainerCmd("$imageName:$imageTag")
            .withHostConfig(
                HostConfig()
                    .withPublishAllPorts(true)
                    .withPrivileged(true)
//                    .withBinds(Bind("/var/run/docker.sock", Volume("/var/run/docker.sock")))
//                    .withMounts(
//                        Mount()
//                            .withSource("/var/run/docker.sock")
//                            .withTarget("/var/run/docker.sock")
//                            .withType(MountType.VOLUME)
//                            .let { listOf(it) }
//                    )
                    .withNetworkMode(network.response.id)
            )
//            .withVolumes(Volume("/var/run/docker.sock"))
            .withExposedPorts(ports)
            .withName("$name-${randomUUID()}")
            .execAsResource(docker)
        allocatedResources.addLast(createdContainer)
        return start(createdContainer, tcpPorts.first(), name)
    }

    private fun start(
        created: CreatedContainer,
        tcpPort: Int,
        name: String
    ): TcpNode {
        val startedContainer = docker
            .startContainerCmd(created.response.id)
            .execAsResource(docker)
        allocatedResources.addLast(startedContainer);
        return install(startedContainer, tcpPort, name)
    }

    private fun install(
        started: StartedContainer,
        tcpPort: Int,
        name: String
    ): TcpNode {
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
        return TcpNode("localhost", ip, tcpPort, name, ssh)
    }

    private fun getHostPort(
        networkSettings: NetworkSettings,
        port: ExposedPort
    ): Int {
        return networkSettings
            .ports
            .bindings[port]!!
            .single { it.hostIp == "0.0.0.0" } // include just the IP4 bind
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