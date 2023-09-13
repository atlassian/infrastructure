package com.atlassian.performance.tools.infrastructure.api.loadbalancer

import java.net.URI
import java.time.Duration

@Deprecated("Use com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode instead.")
interface LoadBalancer {
    fun waitUntilHealthy(timeout: Duration)
    val uri: URI
}
