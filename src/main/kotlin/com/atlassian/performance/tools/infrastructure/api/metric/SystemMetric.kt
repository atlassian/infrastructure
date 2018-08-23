package com.atlassian.performance.tools.infrastructure.api.metric

import java.time.Instant

data class SystemMetric(
    val start: Instant,
    val dimension: Dimension,
    val value: Double,
    val system: String
)