package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class AdoptOpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(AdoptOpenJDK11()).shouldSupportJstat()
    }

    @Test
    fun shouldHaveJavaHomeSet() {
        JdkSupport(AdoptOpenJDK11()).shouldHaveJavaHomeSet("/jdk-11.0.1+13")
    }

    @Test
    fun shouldLoadFont() {
        JdkSupport(AdoptOpenJDK11()).shouldLoadFont()
    }

}
