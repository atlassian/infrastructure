package com.atlassian.performance.tools.infrastructure.api.browser.chromium

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class Chromium70IT {

    @Test
    fun shouldThrowUnsupportedError() {
        SshUbuntuContainer.Builder().build().start().use { sshUbuntu ->
            sshUbuntu.toSsh().newConnection().use { connection ->
                val exception = Assert.assertThrows(IllegalStateException::class.java) {
                    Chromium70().install(connection)
                }

                Assert.assertThat(
                    exception.message,
                    Matchers.containsString("is no longer supported")
                )
                Assert.assertThat(
                    exception.message,
                    Matchers.containsString("CustomChromium")
                )
            }
        }
    }
}
