package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class TarGzJdkIT {
    private val jdk = TarGzJdk.Builder(
        majorVersion = "17",
        minorVersion = "0",
        patchVersion = "11_9",
        downloadUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_x64_linux_hotspot_17.0.11_9.tar.gz"
    )

    @Test
    fun shouldSupportJstat() {
        JstatSupport(jdk.build()).shouldSupportJstat()
    }

    @Test
    fun shouldGatherThreadDump() {
        ThreadDumpTest(jdk.build()).shouldGatherThreadDump()
    }

    @Test
    fun shouldHaveJavaHomeSet() {
        JdkSupport(jdk.build()).shouldHaveJavaHomeSet("/jdk-17.0.11_9")
    }

}
