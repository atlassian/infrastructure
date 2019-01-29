package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class AdoptOpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        val expectedJstatHeader = "Timestamp         S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT   "
        JstatSupport(AdoptOpenJDK11(), expectedJstatHeader).shouldSupportJstat()
    }
}