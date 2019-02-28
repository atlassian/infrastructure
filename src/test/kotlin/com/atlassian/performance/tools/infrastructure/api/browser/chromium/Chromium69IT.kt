package com.atlassian.performance.tools.infrastructure.api.browser.chromium

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class Chromium69IT {

    @Test
    fun shouldInstallBrowser() {
        SshUbuntuContainer().start().use { sshUbuntu ->
            sshUbuntu.toSsh().newConnection().use { connection ->
                val installedBefore = isChromiumInstalled(connection)

                Chromium69().install(connection)

                val installedAfter = isChromiumInstalled(connection)

                Assert.assertThat(installedBefore, Matchers.`is`(false))
                Assert.assertThat(installedAfter, Matchers.`is`(true))
            }
        }
    }

    @Test
    fun shouldRecoverFromPageLoadTimeout() {
        PageLoadTimeoutRecoveryTest().run(Chromium69())
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
