package com.atlassian.performance.tools.infrastructure.browser

import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.virtualusers.api.browsers.Browser
import com.atlassian.performance.tools.virtualusers.api.browsers.CloseableRemoteWebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.rnorth.ducttape.unreliables.Unreliables.retryUntilSuccess
import java.net.URI
import java.util.concurrent.TimeUnit

internal class SshChromium(
    private val ssh: SshConnection,
    private val chromedriverUri: URI
) : Browser {
    override fun start(): CloseableRemoteWebDriver {
        val chromedriverProcess: DetachedProcess = ssh.startProcess("./chromedriver --whitelisted-ips")

        val chromeOptions = ChromeOptions()
            .apply { addArguments("--headless") }
            .apply { addArguments("--no-sandbox") }
            .apply { addArguments("--disable-infobars") }
            .setExperimentalOption(
                "prefs",
                mapOf(
                    "credentials_enable_service" to false
                )
            )
        val driver = retryUntilSuccess(20, TimeUnit.SECONDS) {
            RemoteWebDriver(chromedriverUri.toURL(), chromeOptions)
        }
        return object : CloseableRemoteWebDriver(driver) {
            override fun close() {
                super.close()
                ssh.stopProcess(chromedriverProcess)
            }
        }
    }
}