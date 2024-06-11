package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class TarGzJdkIT {
    private val jdk = TarGzJdk.Builder().build()

    @Test
    fun shouldSupportJstat() {
        JstatSupport(jdk).shouldSupportJstat()
    }

    @Test
    fun shouldGatherThreadDump() {
        ThreadDumpTest(jdk).shouldGatherThreadDump()
    }

    @Test
    fun shouldHaveJavaHomeSet() {
        JdkSupport(jdk).shouldHaveJavaHomeSet("/jdk-17.0.11+9")
    }

}
