package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class OpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(OpenJDK()).shouldSupportJstat()
    }

    @Test
    fun shouldHaveJavaHome() {
        JdkSupport(OpenJDK()).shouldHaveJavaHomeSet("/usr/lib/jvm/java-1.8.0-openjdk-")
    }

    @Test
    fun shouldLoadFont() {
        JdkSupport(OpenJDK()).shouldLoadFont()
    }

}
