package com.atlassian.performance.tools.infrastructure.api.browser.chromium

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage
import com.atlassian.performance.tools.infrastructure.api.jvm.StaticBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import java.time.Duration

class Chromium69IT {

    @Test
    fun shouldInstallBrowser() {
        SshUbuntuImage.runSoloSsh { ssh ->
            val installedBefore = isChromiumInstalled(ssh)

            Chromium69().install(ssh)

            val installedAfter = IdempotentAction("Find Chromium") {
                isChromiumInstalled(ssh)
            }.retry(maxAttempts = 2, backoff = StaticBackoff(Duration.ofSeconds(1)))
            Assert.assertThat(installedBefore, Matchers.`is`(false))
            Assert.assertThat(installedAfter, Matchers.`is`(true))
        }
    }

    private fun isChromiumInstalled(ssh: SshConnection): Boolean {
        val result = ssh.safeExecute("ls -lh /usr/bin/chrome")
        return result.isSuccessful() and result.output.contains("/root/chrome-linux/chrome")
    }
}
