package com.atlassian.performance.tools.infrastructure.api.browser.chromium

import org.junit.Test

class Chromium70IT {

    @Test
    fun shouldRecoverFromPageLoadTimeout() {
        PageLoadTimeoutRecoveryTest().run(Chromium70())
    }
}
