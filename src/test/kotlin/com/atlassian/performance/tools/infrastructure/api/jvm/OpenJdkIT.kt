package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class OpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(OpenJDK()).shouldSupportJstat()
    }
}