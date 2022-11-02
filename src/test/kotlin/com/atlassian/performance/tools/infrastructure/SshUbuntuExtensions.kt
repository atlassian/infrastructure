package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntu
import java.time.Duration

internal fun SshUbuntu.toSsh(): Ssh {
    val ssh = Ssh(this.ssh)
    ssh.newConnection().use { connection ->
        connection.execute("apt-get update -qq", Duration.ofMinutes(3))
        connection.execute("export DEBIAN_FRONTEND=noninteractive; apt-get install sudo curl screen gnupg2 -y -qq", Duration.ofMinutes(10))
    }
    return ssh
}
