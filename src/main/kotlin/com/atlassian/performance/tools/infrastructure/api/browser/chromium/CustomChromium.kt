package com.atlassian.performance.tools.infrastructure.api.browser.chromium

import com.atlassian.performance.tools.infrastructure.ChromedriverInstaller
import com.atlassian.performance.tools.infrastructure.ChromiumInstaller
import com.atlassian.performance.tools.infrastructure.api.browser.Browser
import com.atlassian.performance.tools.infrastructure.ParallelExecutor
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class CustomChromium(
    private val chromiumUri: URI,
    private val chromedriverUri: URI
) : Browser {

    /**
     * Installs chromium with specified URI
     */
    override fun install(ssh: SshConnection) {
        ParallelExecutor().execute(
            { ChromiumInstaller(chromiumUri).install(ssh) },
            { ChromedriverInstaller(chromedriverUri).install(ssh) }
        )
    }
}