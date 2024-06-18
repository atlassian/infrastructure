package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class VersionedOracleJdkIT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(VersionedOracleJdk.Builder().build()).shouldSupportJstat()
    }

    @Test
    fun shouldGatherThreadDump() {
        ThreadDumpTest(VersionedOracleJdk.Builder().build()).shouldGatherThreadDump()
    }

    @Test
    fun shouldHaveJavaHomeSet() {
        JdkSupport(VersionedOracleJdk.Builder().build()).shouldHaveJavaHomeSet("/jdk-17.0.11")
    }

    @Test
    fun shouldHaveJavaHomeSetForJdk21() {
        JdkSupport(
            VersionedOracleJdk.Builder()
                .version("21", "0", "2")
                .build()
        ).shouldHaveJavaHomeSet("/jdk-21.0.2")
    }

    @Test
    fun shouldLoadFont() {
        JdkSupport(VersionedOracleJdk.Builder().build()).shouldLoadFont()
    }

}
