package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNode
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer

internal class DockerInfrastructure : Infrastructure {

    private val allocatedResources: Queue<AutoCloseable> = ConcurrentLinkedQueue()

    override fun serve(jiraNodePlans: List<JiraNodePlan>): List<JiraNode> {
        return jiraNodePlans.mapIndexed { nodeIndex, plan ->
            plan.materialize(serve(8080, "jira-node-$nodeIndex"))
        }
    }

    override fun serve(port: Int, name: String): TcpHost {
        val container = SshUbuntuContainer(Consumer {
            it.addExposedPort(port)
            it.setPortBindings(listOf("$port:$port"))
            it.setPrivilegedMode(true)
        })
        val sshUbuntu = container.start()
        allocatedResources.add(sshUbuntu)
        return TcpHost(
            "localhost",
            port,
            name,
            sshUbuntu.toSsh()
        )
    }

    override fun close() {
        while (true) {
            allocatedResources
                .poll()
                ?.use {}
                ?: break
        }
    }
}