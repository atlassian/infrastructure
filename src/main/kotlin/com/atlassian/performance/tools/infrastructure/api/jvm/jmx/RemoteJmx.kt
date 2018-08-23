package com.atlassian.performance.tools.infrastructure.api.jvm.jmx

import com.atlassian.performance.tools.infrastructure.api.jvm.JvmArg

interface RemoteJmx {
    fun getJvmArgs(ip: String): List<JvmArg>
    fun getClient(ip: String): JmxClient
    fun getRequiredPorts(): List<Int>
}