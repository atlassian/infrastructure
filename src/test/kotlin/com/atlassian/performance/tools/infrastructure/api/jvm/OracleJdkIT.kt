package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.junit.Test

class OracleJdkIT {

    @Test
    fun shouldSupportJstatAndThreadDumps() {
        DockerInfrastructure().use { infra ->
            infra.serve().newConnection().use { connection ->
                val jdk = OracleJDK()
                JstatSupport(jdk).shouldSupportJstat(connection)
                ThreadDumpTest().shouldGatherThreadDump(jdk, connection)
            }
        }
    }
}