package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class Chromium(private val version : String) : Browser {
    override fun install(ssh: SshConnection) {
        val ubuntu = Ubuntu()
        ubuntu.install(ssh, listOf("chromium-browser=$version"), Duration.ofMinutes(2))
    }
}