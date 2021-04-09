package com.atlassian.performance.tools.infrastructure.lib.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.StartContainerCmd

class StartedContainer(
    private val docker: DockerClient,
    val id: String
) : AutoCloseable {
    override fun close() {
        docker.stopContainerCmd(id).exec()
    }
}

fun StartContainerCmd.execAsResource(
    docker: DockerClient
): StartedContainer {
    exec()
    return StartedContainer(
        docker,
        containerId!!
    )
}
