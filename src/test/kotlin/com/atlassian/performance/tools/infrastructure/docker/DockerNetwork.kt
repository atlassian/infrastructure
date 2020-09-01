package com.atlassian.performance.tools.infrastructure.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateNetworkCmd
import com.github.dockerjava.api.command.CreateNetworkResponse

class DockerNetwork(
    private val docker: DockerClient,
    val response: CreateNetworkResponse
) : AutoCloseable {
    override fun close() {
        docker.removeNetworkCmd(response.id).exec()
    }
}

fun CreateNetworkCmd.execAsResource(
    docker: DockerClient
): DockerNetwork = DockerNetwork(docker, exec())