package com.atlassian.performance.tools.infrastructure.jvm.jmx

import javax.management.remote.JMXConnector

interface JmxClient {
    fun <T> execute(consumer: (JMXConnector) -> T)
}