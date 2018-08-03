package com.atlassian.performance.tools.infrastructure.virtualusers

import java.time.Duration

/**
 * Describes load for performance test.
 *
 * @param virtualUsersPerNode number of virtual users to run per node
 * @param loadSchedule to be applied
 * @param rampUpInterval between virtual users within a node
 * @param seed random seed to be used by virtual users
 */
data class LoadProfile(
    val virtualUsersPerNode: Int,
    val loadSchedule: GrowingLoadSchedule,
    val rampUpInterval: Duration = Duration.ofSeconds(0),
    val seed: Long
) {
    fun maxExpectedVirtualUsers() = virtualUsersPerNode * loadSchedule.finalNodes
}