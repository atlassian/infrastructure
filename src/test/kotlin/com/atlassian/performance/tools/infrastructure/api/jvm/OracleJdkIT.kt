package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class OracleJdkIT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(OracleJDK()).shouldSupportJstat()
    }

    @Test
    fun shouldGatherThreadDump() {
        ThreadDumpTest(OracleJDK()).shouldGatherThreadDump()
    }

    @Test
    fun shouldHaveJavaHomeSet() {
        JdkSupport(OpenJDK()).shouldHaveJavaHomeSet("/jdk1.8.0_131")
    }
}
