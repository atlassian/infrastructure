package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test


class ChromeIT {

    @Test
    fun shouldInstallChromeBrowser() {
        DockerInfrastructure().use { infra ->
            infra.serve(80, "ChromeIT").ssh.newConnection().use { connection ->
                val wasInstalledBefore = isChromeInstalled(connection)

                Chrome().install(connection)

                val isInstalledAfter = isChromeInstalled(connection)

                Assert.assertThat(wasInstalledBefore, Matchers.`is`(false))
                Assert.assertThat(isInstalledAfter, Matchers.`is`(true))
                Assert.assertThat(isChromedriverInstalled(connection), Matchers.`is`(true))
            }
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
