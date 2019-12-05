package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.concurrency.api.submitWithLogContext
import com.atlassian.performance.tools.virtualusers.api.VirtualUserLoad
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserBehavior
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.apache.logging.log4j.LogManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.temporal.ChronoUnit.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class MulticastVirtualUsers<out T : VirtualUsers>(
    val nodes: List<T>
) : VirtualUsers {
    private val logger = LogManager.getLogger(this::class.java)

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
        operation: (T, Int) -> Unit
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
        logEstimatedFinish(load, options, nodeCount)
        applySlicedLoad(loadSlices, options)
    }

    private fun logEstimatedFinish(load: VirtualUserLoad, options: VirtualUserOptions, nodeCount: Int) {
        val estimatedDuration = load.total + options.behavior.maxOverhead
        val durationText = "~${estimatedDuration.toMinutes()}m"
        val finishText = LocalDateTime.now().plus(estimatedDuration).truncatedTo(MINUTES).format(ISO_LOCAL_TIME)
        logger.info("Applying load using $nodeCount nodes for $durationText, should finish by $finishText...")
    }

    private fun applySlicedLoad(
        loadSlices: List<VirtualUserLoad>,
        options: VirtualUserOptions
    ) {
        val activeNodes = ConcurrentHashMap.newKeySet<T>()
        multicast("apply load") { node, index ->
            activeNodes.add(node)
            val nodeOptions = VirtualUserOptions(
                target = options.target,
                behavior = VirtualUserBehavior.Builder(options.behavior)
                    .load(loadSlices[index])
                    .let { if (index > 0) it.skipSetup(true) else it }
                    .build()
            )
            try {
                node.applyLoad(nodeOptions)
            } finally {
                activeNodes.remove(node)
                logger.info("Remaining active virtual user nodes: ${activeNodes.size}")
                logger.debug("Remaining active virtual user nodes: $activeNodes")
            }
        }
    }
}
