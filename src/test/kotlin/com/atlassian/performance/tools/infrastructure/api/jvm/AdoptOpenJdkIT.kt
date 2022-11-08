package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class AdoptOpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(AdoptOpenJDK()).shouldSupportJstat()
    }
}