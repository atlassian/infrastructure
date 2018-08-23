package com.atlassian.performance.tools.infrastructure.api.loadbalancer

import java.net.URI
import java.time.Duration

interface LoadBalancer {
    fun waitUntilHealthy(timeout: Duration)
    val uri: URI
}