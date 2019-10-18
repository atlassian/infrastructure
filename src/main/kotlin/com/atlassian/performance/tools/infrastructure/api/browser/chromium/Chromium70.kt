package com.atlassian.performance.tools.infrastructure.api.browser.chromium

import com.atlassian.performance.tools.infrastructure.ChromedriverInstaller
import com.atlassian.performance.tools.infrastructure.ChromiumInstaller
import com.atlassian.performance.tools.infrastructure.ParallelExecutor
import com.atlassian.performance.tools.infrastructure.api.browser.Browser
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class Chromium70 : Browser {
    private val chromiumUri = URI("https://www.googleapis.com/download/storage/v1/b/chromium-browser-snapshots/o/Linux_x64%2F587811%2Fchrome-linux.zip?generation=1535668921668411&alt=media")
    private val chromedriverUri = URI("https://s3.eu-central-1.amazonaws.com/jpt-chromedriver/2.43/chromedriver.zip")

    /**
     * Installs chromium 70 with a compatible chromedriver.
     */
    override fun install(ssh: SshConnection) {
        ParallelExecutor().execute(
            { ChromiumInstaller(chromiumUri).install(ssh) },
            { ChromedriverInstaller(chromedriverUri).install(ssh) }
        )
    }
}