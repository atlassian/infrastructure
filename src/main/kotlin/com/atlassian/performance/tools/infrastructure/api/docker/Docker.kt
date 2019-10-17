package com.atlassian.performance.tools.infrastructure.api.docker

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class Docker private constructor(
    private val dependencyPackagesTimeout: Duration,
    private val mainPackageTimeout: Duration
) {

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
                "curl"
            ),
            timeout = dependencyPackagesTimeout
        )
        ubuntu.addKey(ssh, "7EA0A9C3F273FCD8")

        val release = ubuntu.getDistributionCodename(ssh)
        ubuntu.addRepository(ssh, "deb [arch=amd64] https://download.docker.com/linux/ubuntu $release stable", "docker");

        val version = "5:19.03.13~3-0~ubuntu-$release"
        ubuntu.install(
            ssh = ssh,
            packages = listOf("docker-ce=$version"),
            timeout = mainPackageTimeout
        )
        ssh.execute("sudo service docker status || sudo service docker start")
    }

    class Builder {

        private var dependencyPackagesTimeout: Duration = Duration.ofMinutes(2)
        private var mainPackageTimeout: Duration = Duration.ofMinutes(5)

        fun build(): Docker = Docker(
            dependencyPackagesTimeout,
            mainPackageTimeout
        )
    }
}
