package com.atlassian.performance.tools.infrastructure.jvm

import com.atlassian.performance.tools.infrastructure.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.ssh.SshConnection

internal class PreinstalledJDK(
    private val javaBin: String
) : JavaDevelopmentKit {
    override fun install(connection: SshConnection) {}

    override fun use(): String {
        return ""
    }

    override fun command(options: String): String {
        return "$javaBin $options"
    }
}