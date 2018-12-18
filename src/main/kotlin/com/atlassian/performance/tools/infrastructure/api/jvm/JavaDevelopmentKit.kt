package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.ssh.api.SshConnection

interface JavaDevelopmentKit {
    fun install(connection: SshConnection)
    fun use(): String
    fun command(options: String): String
    val jstatMonitoring: Jstat
}