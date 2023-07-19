package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.ChromeForTesting
import com.atlassian.performance.tools.infrastructure.ChromedriverInstaller
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration.ofMinutes

/**
 * We have no control over the Chrome version. We install the latest stable Chrome version. It may cause not repeatable builds.
 */
class Chrome : Browser {
    private val ubuntu = Ubuntu()

    override fun install(ssh: SshConnection) {
        ubuntu.addKey(ssh, "78BD65473CB3BD13")
        ubuntu.addRepository(ssh, "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main", "google-chrome")
        Ubuntu().install(ssh, listOf("google-chrome-stable"), ofMinutes(5))
        val installedMinorVersion = getInstalledBuildVersion(ssh)

        val uri = if (installedMinorVersion != null) {
            ChromeForTesting.getLatestDownloadUri(installedMinorVersion)
        } else {
            ChromeForTesting.getLatestStableDownloadUri()
        }
        ChromedriverInstaller(uri).install(ssh)
    }

    /**
     * https://chromedriver.chromium.org/downloads/version-selection
     *
     * > ChromeDriver uses the same version number scheme as Chrome. See https://www.chromium.org/developers/version-numbers for more details.
     * > Each version of ChromeDriver supports Chrome with matching major, minor, and build version numbers. For example, ChromeDriver 73.0.3683.20 supports all Chrome versions that start with 73.0.3683.
     */
    private fun getInstalledBuildVersion(ssh: SshConnection): String? {
        val versionString = ssh.execute("/usr/bin/google-chrome --version").output
        return Regex("Google Chrome ([0-9]+\\.[0-9]+\\.[0-9]+)\\.[0-9]+").find(versionString)?.groupValues?.getOrNull(1)
    }
}
