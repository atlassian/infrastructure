package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.docker.SshUbuntuContainer
import org.assertj.core.api.Assertions
import org.junit.Test
import java.net.URI

class ChromedriverInstallerTest {
    private val version = "2.45"

    @Test
    fun shouldInstallChromedriver() {
        SshUbuntuContainer().run { ssh ->
            val chromedriverUri = URI("https://chromedriver.storage.googleapis.com/$version/chromedriver_linux64.zip")
            ChromedriverInstaller(chromedriverUri).install(ssh)
            val result = ssh.safeExecute("./chromedriver --version")
            Assertions.assertThat(result.isSuccessful()).isTrue()
            Assertions.assertThat(result.output).contains(version)
        }
    }
}