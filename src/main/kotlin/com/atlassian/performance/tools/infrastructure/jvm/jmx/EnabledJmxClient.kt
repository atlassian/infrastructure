package com.atlassian.performance.tools.infrastructure.jvm.jmx

import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

class EnabledJmxClient(
    private val ip: String,
    private val port: Int
) : JmxClient {
    override fun <T> execute(
        consumer: (JMXConnector) -> T
    ) {
        val url = JMXServiceURL("service:jmx:rmi:///jndi/rmi://$ip:$port/jmxrmi")
        JMXConnectorFactory.connect(url).use { consumer(it) }
    }
}