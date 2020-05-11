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
        ubuntu.addKey(ssh, "7EA0A9C3F273FCD8")

        val release = ssh.execute("lsb_release -cs").output
        ubuntu.addRepository(ssh, "deb [arch=amd64] https://download.docker.com/linux/ubuntu $release stable");

        val version = "5:19.03.8~3-0~ubuntu-$release"
        ubuntu.install(
            ssh = ssh,
            packages = listOf("docker-ce=$version"),
            timeout = Duration.ofMinutes(5)
        )
    }
}
