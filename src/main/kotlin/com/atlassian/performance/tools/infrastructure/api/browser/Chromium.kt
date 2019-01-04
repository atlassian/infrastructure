package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.api.browser.chromium.Chromium69
import com.atlassian.performance.tools.infrastructure.api.browser.chromium.Chromium70
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

@Deprecated(message = "Use version specific Chromium. For example `Chromium69`.")
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
            throw IllegalArgumentException("Chromium version '$version' is not supported.")
        }
    }

    override fun install(ssh: SshConnection) {
        when (version) {
            "69" -> Chromium69().install(ssh)
            "70" -> Chromium70().install(ssh)
            else -> throw IllegalArgumentException("Chromium version '$version' is not supported.")
        }
    }
}