package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class OracleJdkIT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(OracleJDK()).shouldSupportJstat()
    }
}