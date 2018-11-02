package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.concurrency.api.submitWithLogContext
import com.atlassian.performance.tools.virtualusers.api.VirtualUserLoad
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
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
        operation: (VirtualUsers, Long) -> Unit
    ) {
        val executor = Executors.newFixedThreadPool(
            nodes.size,
            ThreadFactoryBuilder()
                .setNameFormat("multicast-virtual-users-$label-thread-%d")
                .build()
        )
        nodes
            .mapIndexed { index, node ->
                executor.submitWithLogContext("$label $node") {
                    try {
                        operation(node, index.toLong())
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
        val load = options.virtualUserLoad
        val virtualUsers = load.virtualUsers
        if (nodeCount > virtualUsers) {
            throw Exception("$virtualUsers virtual users are not enough to spread into $nodeCount nodes")
        }
        val vusPerNode = virtualUsers / nodeCount
        val rampPerNode = load.ramp.dividedBy(nodeCount.toLong())
        multicast("apply load") { node, index ->
            node.applyLoad(
                VirtualUserOptions(
                    virtualUserLoad = VirtualUserLoad(
                        virtualUsers = vusPerNode,
                        hold = load.hold + rampPerNode.multipliedBy(index),
                        ramp = rampPerNode,
                        flat = load.flat + rampPerNode.multipliedBy(nodeCount - index - 1)
                    ),
                    help = false,
                    scenario = options.scenario,
                    seed = options.seed,
                    jiraAddress = options.jiraAddress,
                    adminLogin = options.adminLogin,
                    adminPassword = options.adminPassword,
                    diagnosticsLimit = options.diagnosticsLimit
                )
            )
        }
    }
}