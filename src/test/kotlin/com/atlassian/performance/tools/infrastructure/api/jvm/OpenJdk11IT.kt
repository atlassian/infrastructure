package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class OpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(OpenJDK11()).shouldSupportJstat()
    }

    @Test
    fun shouldHaveJavaHome() {
        JdkSupport(OpenJDK11()).shouldHaveJavaHomeSet("/usr/lib/jvm/java-1.11.0-openjdk-")
    }

    @Test
    fun shouldLoadFont() {
        JdkSupport(OpenJDK11()).shouldLoadFont()
    }

}
