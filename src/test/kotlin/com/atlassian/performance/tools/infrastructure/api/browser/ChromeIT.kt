package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.docker.SshUbuntuContainer
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test


class ChromeIT {

    @Test
    fun shouldInstallChromeBrowser() {
        SshUbuntuContainer().run { ssh ->
            val wasInstalledBefore = isChromeInstalled(ssh)

            Chrome().install(ssh)

            val isInstalledAfter = isChromeInstalled(ssh)

            Assert.assertThat(wasInstalledBefore, Matchers.`is`(false))
            Assert.assertThat(isInstalledAfter, Matchers.`is`(true))
            Assert.assertThat(isChromedriverInstalled(ssh), Matchers.`is`(true))
        }
    }

    private fun isChromeInstalled(ssh: SshConnection): Boolean {
        return ssh
            .execute("apt list google-chrome-stable")
            .output
            .contains("installed")
    }

    private fun isChromedriverInstalled(ssh: SshConnection): Boolean {
        val result = ssh
            .safeExecute("./chromedriver --version")
        return result.isSuccessful()
            .and(
                result
                    .output
                    .contains(Regex("ChromeDriver [0-9]+\\.[0-9]+\\.[0-9]+"))
            )
    }
}