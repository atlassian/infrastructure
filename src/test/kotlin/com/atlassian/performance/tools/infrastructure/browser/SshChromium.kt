package com.atlassian.performance.tools.infrastructure.browser

import com.atlassian.performance.tools.infrastructure.api.jvm.StaticBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.virtualusers.api.browsers.Browser
import com.atlassian.performance.tools.virtualusers.api.browsers.CloseableRemoteWebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.URI
import java.time.Duration

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
        IdempotentAction("Wait for chrome process") {
            waitForChromeProcess(ssh)
        }.retry(maxAttempts = 3, backoff = StaticBackoff(Duration.ofSeconds(5L)))
        val driver = RemoteWebDriver(chromedriverUri.toURL(), chromeOptions)
        return object : CloseableRemoteWebDriver(driver) {
            override fun close() {
                super.close()
                ssh.stopProcess(chromedriverProcess)
            }
        }
    }

    private fun waitForChromeProcess(ssh: SshConnection) {
        ssh.safeExecute("ps aux")
            .output
            .split("\n")
            .filter { it.contains("./chromedriver --whitelisted-ips") }
            .single { it.length < 100 }
    }
}
