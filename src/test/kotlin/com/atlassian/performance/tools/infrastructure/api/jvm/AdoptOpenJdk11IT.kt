package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class AdoptOpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(AdoptOpenJDK11()).shouldSupportJstat()
    }
}