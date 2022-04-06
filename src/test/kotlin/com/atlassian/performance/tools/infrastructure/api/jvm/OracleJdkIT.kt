package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.junit.Test

class OracleJdkIT {

    @Test
    fun shouldSupportJstatAndThreadDumps() {
        val jdk = OracleJDK()
        DockerInfrastructure().use { infra ->
            val ssh = infra.serveTest()
            JstatSupport(jdk, ssh).shouldSupportJstat()
            ThreadDumpTest(jdk, ssh).shouldGatherThreadDump()
        }
    }
}