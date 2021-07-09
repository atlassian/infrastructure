package com.atlassian.performance.tools.infrastructure.browser

import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.virtualusers.api.browsers.Browser
import com.atlassian.performance.tools.virtualusers.api.browsers.CloseableRemoteWebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.URI
import java.time.Duration.ofSeconds

internal class SshChromium(
    private val ssh: Ssh,
    private val chromedriverUri: URI
) : Browser {
    override fun start(): CloseableRemoteWebDriver {
        ssh.runInBackground("./chromedriver --whitelisted-ips")
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
        ssh.newConnection().use { connection ->
            IdempotentAction("Wait for chrome process") {
                waitForChromeProcess(connection)
            }.retry(3, StaticBackoff(ofSeconds(5)))
        }
        val driver = RemoteWebDriver(chromedriverUri.toURL(), chromeOptions)
        return CloseableRemoteWebDriver(driver)
    }

    private fun waitForChromeProcess(ssh: SshConnection) {
        ssh.safeExecute("ps aux")
            .output
            .split("\n")
            .filter { it.contains("./chromedriver --whitelisted-ips") }
            .single { it.length < 100 }
    }
}
