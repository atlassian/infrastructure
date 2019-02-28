package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class ChromiumIT {

    @Test
    fun shouldInstallChromium69Browser() {
        SshUbuntuContainer().start().use { sshUbuntu ->
            sshUbuntu.toSsh().newConnection().use { connection ->
                val wasInstalledBefore = isChromiumInstalled(connection)

                Chromium("69").install(connection)

                val isInstalledAfter = isChromiumInstalled(connection)

                Assert.assertThat(wasInstalledBefore, Matchers.`is`(false))
                Assert.assertThat(isInstalledAfter, Matchers.`is`(true))
            }
        }
    }

    @Test
    fun shouldInstallChromium70Browser() {
        SshUbuntuContainer().start().use { sshUbuntu ->
            sshUbuntu.toSsh().newConnection().use { connection ->
                val wasInstalledBefore = isChromiumInstalled(connection)

                Chromium("70").install(connection)

                val isInstalledAfter = isChromiumInstalled(connection)

                Assert.assertThat(wasInstalledBefore, Matchers.`is`(false))
                Assert.assertThat(isInstalledAfter, Matchers.`is`(true))
            }
        }
    }

    @Test
    fun shouldFailFastOnIncompatibleChromiumVersion() {
        SshUbuntuContainer().start().use { sshUbuntu ->
            sshUbuntu.toSsh().newConnection().use { connection ->
                val thrown = catchThrowable { Chromium("68").install(connection) }

                assertThat(thrown)
                    .hasMessageContaining("is not supported")
            }
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