package com.atlassian.performance.tools.infrastructure.jvm.jmx

import com.atlassian.performance.tools.infrastructure.jvm.JvmArg

class DisabledRemoteJmx : RemoteJmx {
    override fun getJvmArgs(
        ip: String
    ): List<JvmArg> = emptyList()

    override fun getClient(ip: String): JmxClient = DisabledJmxClient()

    override fun getRequiredPorts(): List<Int> = emptyList()
}