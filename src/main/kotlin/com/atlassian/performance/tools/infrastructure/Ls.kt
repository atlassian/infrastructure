package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.api.SshConnection

internal class Ls {
    internal fun execute(
        ssh: SshConnection,
        destination: String
    ): Set<String> {
        return ssh
            .execute("ls $destination")
            .output
            .split("\\s".toRegex())
            .filter { it.isNotBlank() }
            .toSet()
    }
}