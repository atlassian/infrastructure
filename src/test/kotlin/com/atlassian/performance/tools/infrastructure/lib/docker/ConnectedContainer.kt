package com.atlassian.performance.tools.infrastructure.lib.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.ConnectToNetworkCmd

class ConnectedContainer(
    private val docker: DockerClient,
    val containerId: String,
    val networkId: String
) : AutoCloseable {

    override fun close() {
        docker
            .disconnectFromNetworkCmd()
            .withContainerId(containerId)
            .withNetworkId(networkId)
            .exec()
    }
}

fun ConnectToNetworkCmd.execAsResource(
    docker: DockerClient
): ConnectedContainer {
    exec()
    return ConnectedContainer(
        docker,
        containerId!!,
        networkId!!
    )
}
