package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.UbuntuContainer
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test


class ChromeIT {

    @Test
    fun shouldInstallChromeBrowser() {
        UbuntuContainer().run { ssh ->
            val wasInstalledBefore = isChromeInstalled(ssh)

            Chrome().install(ssh)

            val isInstalledAfter = isChromeInstalled(ssh)

            Assert.assertThat(wasInstalledBefore, Matchers.`is`(false))
            Assert.assertThat(isInstalledAfter, Matchers.`is`(true))
        }
    }

    private fun isChromeInstalled(ssh: SshConnection): Boolean {
        return ssh
            .execute("apt list google-chrome-stable")
            .output
            .contains("installed")
    }
}