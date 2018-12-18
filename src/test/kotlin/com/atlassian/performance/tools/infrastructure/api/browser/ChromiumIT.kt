package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.UbuntuContainer
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class ChromiumIT {

    @Test
    fun shouldInstallChromium69Browser() {
        UbuntuContainer().run { ssh ->
            val wasInstalledBefore = isChromiumInstalled(ssh)

            Chromium("69").install(ssh)

            val isInstalledAfter = isChromiumInstalled(ssh)

            Assert.assertThat(wasInstalledBefore, Matchers.`is`(false))
            Assert.assertThat(isInstalledAfter, Matchers.`is`(true))
        }
    }

    @Test
    fun shouldInstallChromium70Browser() {
        UbuntuContainer().run { ssh ->
            val wasInstalledBefore = isChromiumInstalled(ssh)

            Chromium("70").install(ssh)

            val isInstalledAfter = isChromiumInstalled(ssh)

            Assert.assertThat(wasInstalledBefore, Matchers.`is`(false))
            Assert.assertThat(isInstalledAfter, Matchers.`is`(true))
        }
    }

    @Test
    fun shouldFailFastOnIncompatibleChromiumVersion() {
        UbuntuContainer().run { ssh ->
            val thrown = catchThrowable { Chromium("68").install(ssh) }

            assertThat(thrown)
                .hasMessageContaining("is not supported")
        }
    }

    private fun isChromiumInstalled(ssh: SshConnection): Boolean {
        val result = ssh
            .safeExecute("ls -lh /usr/bin/chrome")
        return result.isSuccessful()
            .and(
                result
                    .output
                    .contains("/root/chrome-linux/chrome")
            )
    }
}