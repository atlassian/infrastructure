package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.concurrency.api.submitWithLogContext
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserBehavior
import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.Executors

class MulticastVirtualUsers<out T : VirtualUsers>(
    val nodes: List<T>
) : VirtualUsers {

    override fun gatherResults() {
        multicast("gatherResults") { node, _ ->
            node.gatherResults()
        }
    }

    /**
     * @param label Labels the [operation].
     * @param operation Operates on a the given node, indexed from 0.
     */
    private fun multicast(
        label: String,
        operation: (VirtualUsers, Int) -> Unit
    ) {
        val executor = Executors.newFixedThreadPool(
            nodes.size,
            ThreadFactoryBuilder()
                .setNameFormat("multicast-virtual-users-$label-thread-%d")
                .build()
        )
        nodes
            .mapIndexed { index, node ->
                executor.submitWithLogContext("$label node-${index + 1}") {
                    try {
                        operation(node, index)
                    } catch (e: Exception) {
                        throw Exception("$label failed on $node", e)
                    }
                }
            }
            .forEach { it.get() }
        executor.shutdownNow()
    }

    override fun applyLoad(options: VirtualUserOptions) {
        val nodeCount = nodes.size
        val load = options.behavior.load
        val virtualUsers = load.virtualUsers
        if (nodeCount > virtualUsers) {
            throw Exception("$virtualUsers virtual users are not enough to spread into $nodeCount nodes")
        }
        val loadSlices = load.slice(nodeCount)
        multicast("apply load") { node, index ->
            node.applyLoad(
                VirtualUserOptions(
                    target = options.target,
                    behavior = VirtualUserBehavior.Builder(options.behavior)
                        .load(loadSlices[index])
                        .let { if (index > 0) it.skipSetup(true) else it }
                        .build()
                )
            )
        }
    }
}
