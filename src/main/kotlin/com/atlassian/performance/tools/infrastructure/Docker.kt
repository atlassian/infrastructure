package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

internal class Docker {

    private val ubuntu = Ubuntu()

    /**
     * See the [official guide](https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/#install-docker-ce).
     */
    fun install(
        ssh: SshConnection
    ) {
        ubuntu.install(
            ssh = ssh,
            packages = listOf(
                "apt-transport-https",
                "ca-certificates",
                "curl",
                "software-properties-common"
            ),
            timeout = Duration.ofMinutes(2)
        )
        val release = ssh.execute("lsb_release -cs").output
        val repository = "deb [arch=amd64] https://download.docker.com/linux/ubuntu $release stable"
        ssh.execute("sudo add-apt-repository \"$repository\"")
        ssh.execute("curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -")
        val version = "17.09.0~ce-0~ubuntu"
        ubuntu.install(
            ssh = ssh,
            packages = listOf("docker-ce=$version"),
            timeout = Duration.ofSeconds(90)
        )
    }
}