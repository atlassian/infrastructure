package com.atlassian.performance.tools.infrastructure.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerCmd
import com.github.dockerjava.api.command.CreateContainerResponse

class CreatedContainer(
    private val docker: DockerClient,
    val response: CreateContainerResponse
) : AutoCloseable {
    override fun close() {
        docker.removeContainerCmd(response.id).exec()
    }
}

fun CreateContainerCmd.execAsResource(
    docker: DockerClient
): CreatedContainer = CreatedContainer(docker, exec())
