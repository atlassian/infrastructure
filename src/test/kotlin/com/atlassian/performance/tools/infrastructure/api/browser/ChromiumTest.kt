package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.UbuntuContainer
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test


class ChromiumTest {

    @Test
    fun shouldInstallChromiumBrowser() {
        UbuntuContainer().run { ssh ->
            val wasInstalledBefore = isChromiumInstalled(ssh)

            Chromium("70.0.3538.67-0ubuntu0.16.04.1").install(ssh)

            val isInstalledAfter = isChromiumInstalled(ssh)

            Assert.assertThat(wasInstalledBefore, Matchers.`is`(false))
            Assert.assertThat(isInstalledAfter, Matchers.`is`(true))
        }
    }

    private fun isChromiumInstalled(ssh: SshConnection): Boolean {
        return ssh
            .execute("apt list chromium-browser")
            .output
            .contains("installed")
    }

}