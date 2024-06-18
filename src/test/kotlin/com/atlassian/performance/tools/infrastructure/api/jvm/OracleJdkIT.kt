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
        JdkSupport(OracleJDK()).shouldHaveJavaHomeSet("/jdk1.8.0_131")
    }

    @Test
    fun shouldLoadFont() {
        JdkSupport(OracleJDK()).shouldLoadFont()
    }

}
