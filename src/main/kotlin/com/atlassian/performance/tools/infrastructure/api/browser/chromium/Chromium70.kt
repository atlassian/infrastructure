package com.atlassian.performance.tools.infrastructure.api.browser.chromium

import com.atlassian.performance.tools.infrastructure.api.browser.Browser
import com.atlassian.performance.tools.ssh.api.SshConnection

@Deprecated("Use CustomChromium")
class Chromium70 : Browser {
    /**
     * Installs chromium 70 with a compatible chromedriver.
     */
    override fun install(ssh: SshConnection) {
        throw IllegalStateException("${this.javaClass} is no longer supported. " +
            "Please use com/atlassian/performance/tools/infrastructure/api/browser/chromium/CustomChromium.kt instead " +
            "with explicitly provided Chromium and Chrome Driver URIs.")
    }
}
