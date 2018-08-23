package com.atlassian.performance.tools.infrastructure.api.jvm

class DisabledJvmDebug : JvmDebug {

    override fun getRequiredPorts(): List<Int> {
        return emptyList()
    }

    override fun getJvmOption(): List<JvmArg> {
        return emptyList()
    }
}