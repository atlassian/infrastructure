package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntu
import java.time.Duration

internal fun SshUbuntu.toSsh(): Ssh {
    val ssh = Ssh(with(this.ssh) {
        com.atlassian.performance.tools.ssh.api.SshHost(
            ipAddress = ipAddress,
            userName = userName,
            authentication = com.atlassian.performance.tools.ssh.api.auth.PublicKeyAuthentication(privateKey),
            port = port
        )
    })
    ssh.newConnection().use { connection ->
        connection.execute("apt-get update -qq", Duration.ofMinutes(3))
        connection.execute("export DEBIAN_FRONTEND=noninteractive; apt-get install sudo curl screen gnupg2 -y -qq", Duration.ofSeconds(60))
    }
    return ssh
}
