package com.atlassian.performance.tools.infrastructure.jvm.jmx

import javax.management.remote.JMXConnector

class DisabledJmxClient : JmxClient {
    override fun <T> execute(consumer: (JMXConnector) -> T) {
        throw UnsupportedOperationException("Remote JMX not enabled")
    }
}