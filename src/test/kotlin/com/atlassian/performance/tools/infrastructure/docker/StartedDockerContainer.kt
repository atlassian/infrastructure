package com.atlassian.performance.tools.infrastructure.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.StartContainerCmd

class StartedDockerContainer(
    private val docker: DockerClient,
    val id: String
) : AutoCloseable {
    override fun close() {
        docker.stopContainerCmd(id).exec()
    }
}

fun StartContainerCmd.execAsResource(
    docker: DockerClient
): StartedDockerContainer {
    exec()
    return StartedDockerContainer(
        docker,
        containerId!!
    )
}
