package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.ssh.SshConnection

interface JavaDevelopmentKit {
    fun install(connection: SshConnection)
    fun use(): String
    fun command(options: String): String
}