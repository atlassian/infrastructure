package com.atlassian.performance.tools.infrastructure.loadbalancer

import com.atlassian.performance.tools.ssh.Ssh
import java.net.URI
import java.time.Duration

class ApacheLoadBalancer(
    private val nodes: List<URI>,
    private val ssh: Ssh,
    private val httpPort: Int
) : LoadBalancer {
    override val uri: URI = URI("http://${ssh.host.ipAddress}:$httpPort/")

    override fun waitUntilHealthy(
        timeout: Duration
    ) {
        throw Exception(
            "TODO: Install Apache load balancer via $ssh," +
                " configure it to balance between $nodes" +
                " and listen on $httpPort"
        )
    }

}