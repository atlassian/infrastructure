package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.api.jvm.Jstat
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class PreinstalledJDK(
    private val javaBin: String,
    override val jstatMonitoring: Jstat
) : JavaDevelopmentKit {
    override fun install(connection: SshConnection) {}

    override fun use(): String {
        return ""
    }

    override fun command(options: String): String {
        return "$javaBin $options"
    }
}