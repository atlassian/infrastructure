package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class OracleJdkIT {

    @Test
    fun shouldSupportJstatAndThreadDumps() {
        val jdk = OracleJDK()
        JstatSupport(jdk).shouldSupportJstat()
        ThreadDumpTest(jdk).shouldGatherThreadDump()
    }
}