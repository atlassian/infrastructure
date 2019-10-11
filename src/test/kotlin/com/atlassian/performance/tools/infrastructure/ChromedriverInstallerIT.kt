package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions
import org.junit.Ignore
import org.junit.Test
import java.net.URI

class ChromedriverInstallerIT {
    private val version = "2.45"

    @Test
    @Ignore
    fun shouldInstallChromedriver() {
        SshUbuntuContainer().start().use { sshUbuntu ->
            sshUbuntu.toSsh().newConnection().use { connection ->
                val chromedriverUri = URI("https://chromedriver.storage.googleapis.com/$version/chromedriver_linux64.zip")
                ChromedriverInstaller(chromedriverUri).install(connection)
                val result = connection.safeExecute("./chromedriver --version")
                Assertions.assertThat(result.isSuccessful()).isTrue()
                Assertions.assertThat(result.output).contains(version)
            }
        }
    }
}
