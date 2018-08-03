package com.atlassian.performance.tools.infrastructure.jvm

class DisabledJvmDebug : JvmDebug {

    override fun getRequiredPorts(): List<Int> {
        return emptyList()
    }

    override fun getJvmOption(): List<JvmArg> {
        return emptyList()
    }
}