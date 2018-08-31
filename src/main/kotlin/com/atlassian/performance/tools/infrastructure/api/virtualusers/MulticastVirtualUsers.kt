package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.concurrency.submitWithLogContext
import com.atlassian.performance.tools.jiraactions.scenario.Scenario
import com.atlassian.performance.tools.virtualusers.VirtualUserOptions
import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.net.URI
import java.util.concurrent.Executors

class MulticastVirtualUsers<out T : VirtualUsers>(
    val nodes: List<T>
) : VirtualUsers {

    override fun gatherResults() {
        multicast("gatherResults") { it.gatherResults() }
    }

    @Deprecated(message = "Do not use.")
    override fun applyLoad(
        jira: URI,
        loadProfile: LoadProfile,
        scenarioClass: Class<out Scenario>?,
        diagnosticsLimit: Int?
    ) {
        multicast("applyLoad") { it.applyLoad(jira, loadProfile, scenarioClass, diagnosticsLimit) }
    }

    private fun multicast(
        label: String,
        operation: (VirtualUsers) -> Unit
    ) {
        val executor = Executors.newFixedThreadPool(
            nodes.size,
            ThreadFactoryBuilder()
                .setNameFormat("multicast-virtual-users-$label-thread-%d")
                .build()
        )
        nodes
            .map {
                executor.submitWithLogContext("$label $it") {
                    try {
                        operation(it)
                    } catch (e: Exception) {
                        throw Exception("$label failed on $it", e)
                    }
                }
            }
            .forEach { it.get() }
        executor.shutdownNow()
    }

    override fun applyLoad(options: VirtualUserOptions) {
        TODO("not implemented")
    }
}