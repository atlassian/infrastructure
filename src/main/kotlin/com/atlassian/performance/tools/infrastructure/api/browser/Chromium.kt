package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration.ofMinutes

class Chromium(private val version: String) : Browser {

    companion object {
        private val versionToUri = mapOf(
            "69" to URI("https://www.googleapis.com/download/storage/v1/b/chromium-browser-snapshots/o/Linux_x64%2F576753%2Fchrome-linux.zip?generation=1532051976706023&alt=media"),
            "70" to URI("https://www.googleapis.com/download/storage/v1/b/chromium-browser-snapshots/o/Linux_x64%2F587811%2Fchrome-linux.zip?generation=1535668921668411&alt=media")
        )

        private fun getSupportedVersions(): Set<String> {
            return versionToUri.keys
        }
    }

    init {
        if (!versionToUri.keys.contains(version)) {
            throw IllegalArgumentException("Chromium version '$version' is not supported. You can use only chromium compatible with chrome driver v2.43." +
                " Use one of the compatible versions: ${getSupportedVersions().joinToString(", ")}.")
        }
    }

    override fun install(ssh: SshConnection) {
        val ubuntu = Ubuntu()
        ubuntu.install(
            ssh,
            listOf(
                "unzip",
                "libx11-xcb1",
                "libxcomposite1",
                "libxdamage1",
                "libxi6",
                "libxtst6",
                "libnss3",
                "libcups2",
                "libxss1",
                "libxrandr2",
                "libasound2",
                "libpango1.0",
                "libatk1.0-0",
                "libatk-bridge2.0",
                "libgtk-3-0"
            ),
            ofMinutes(5)
        )
        val uri = versionToUri.get(version)
        ssh.execute("""wget -q "${uri}" -O chromium.zip""", ofMinutes(1))
        ssh.execute("unzip chromium.zip")
        ssh.execute("sudo ln -s `pwd`/chrome-linux/chrome /usr/bin/chrome")
    }
}