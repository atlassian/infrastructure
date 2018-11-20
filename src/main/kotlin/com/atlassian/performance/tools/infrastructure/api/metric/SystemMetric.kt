package com.atlassian.performance.tools.infrastructure.api.metric

import java.time.Instant

class SystemMetric(
    val start: Instant,
    val dimension: Dimension,
    val value: Double,
    val system: String
) {

    override fun toString(): String {
        return "SystemMetric(start=$start, dimension=$dimension, value=$value, system='$system')"
    }
}