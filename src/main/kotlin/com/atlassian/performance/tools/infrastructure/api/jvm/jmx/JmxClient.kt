package com.atlassian.performance.tools.infrastructure.api.jvm.jmx

import javax.management.remote.JMXConnector

interface JmxClient {
    fun <T> execute(consumer: (JMXConnector) -> T)
}