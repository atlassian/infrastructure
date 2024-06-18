package com.atlassian.performance.tools.infrastructure.api.jvm

class EnabledJvmDebug(
    private val port: Int,
    private val suspend: Boolean
) : JvmDebug {

    override fun getRequiredPorts(): List<Int> {
        return listOf(port)
    }

    override fun getJvmOption(): List<JvmArg> {
        val suspendFlag = if (suspend) "y" else "n"
        return listOf(
            JvmArg(
                key = "-agentlib:jdwp=",
                value = "transport=dt_socket,server=y,suspend=$suspendFlag,address=*:$port"
            )
        )
    }
}
