package com.atlassian.performance.tools.infrastructure.virtualusers

import java.time.Duration

/**
 * Defines growing load schedule for virtual users nodes.
 *
 * The load is constant when [initialNodes] is equal [finalNodes].
 *
 * @param duration of the load test
 * @param initialNodes to start the load with, must not be negative
 * @param finalNodes to end the load with, must not be negative
 *
 * @throws Exception if any of [initialNodes], [finalNodes] is negative or [finalNodes] > [initialNodes].
 */
class GrowingLoadSchedule(
    val duration: Duration,
    private val initialNodes: Int = 1,
    val finalNodes: Int
) {
    /**
     * Number of steps in which the load will be increased
     */
    val stepCount: Long = (finalNodes - initialNodes + 1).toLong()

    /**
     * Duration of each load step.
     */
    val step: Duration = duration.dividedBy(stepCount)!!

    init {
        if (initialNodes > finalNodes) {
            throw Exception("Initial nodes can't be higher than final")
        }
        if (initialNodes < 0) {
            throw Exception("Initial nodes can't be negative")
        }
        if (finalNodes < 0) {
            throw Exception("Final nodes can't be negative")
        }
    }

    /**
     * Calculates delay for consecutive [node]
     *
     * @param node order
     * @return [Duration] of the starting delay for the [node]
     */
    fun startingDelay(
        node: Int
    ): Duration {
        if (node < initialNodes) { return Duration.ZERO }
        val stepNum = node - initialNodes
        return step.multipliedBy(stepNum.toLong())
    }

    /**
     * Calculates load duration for consecutive [node]
     *
     * @param node order
     * @return [Duration] of the load for the [node]
     */
    fun loadDuration(
        node: Int
    ): Duration = duration - startingDelay(node)
}