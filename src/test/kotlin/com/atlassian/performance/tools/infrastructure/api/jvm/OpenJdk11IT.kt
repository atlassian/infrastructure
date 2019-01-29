package com.atlassian.performance.tools.infrastructure.api.jvm

import org.junit.Test

class OpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        JstatSupport(OpenJDK11()).shouldSupportJstat()
    }
}