package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class AdoptOpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(AdoptOpenJDK()).shouldSupportJstat()
    }

    @Test
    fun shouldHaveJavaHomeSet() {
        JdkSupport(AdoptOpenJDK()).shouldHaveJavaHomeSet("/jdk8u172-b11")
    }

    @Test
    fun shouldLoadFont() {
        JdkSupport(AdoptOpenJDK()).shouldLoadFont()
    }

}
