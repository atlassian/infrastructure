package com.atlassian.performance.tools.infrastructure.api.jvm.jmx

import com.atlassian.performance.tools.infrastructure.api.jvm.JvmArg
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class EnabledRemoteJmx(
    private val port: Int = 8686
) : RemoteJmx {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun getJvmArgs(
        ip: String
    ) = listOf(
        JvmArg("-Dcom.sun.management.jmxremote"),
        JvmArg("-Dcom.sun.management.jmxremote.port=", port.toString()),
        JvmArg("-Dcom.sun.management.jmxremote.rmi.port=", port.toString()),
        JvmArg("-Dcom.sun.management.jmxremote.authenticate=", "false"),
        JvmArg("-Dcom.sun.management.jmxremote.ssl=", "false"),
        JvmArg("-Djava.rmi.server.hostname=", ip)
    )

    override fun getClient(ip: String): JmxClient {
        logger.debug("JMX access under: $ip:$port")
        return EnabledJmxClient(ip, port)
    }

    override fun getRequiredPorts(): List<Int> = listOf(port)
}