package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

internal class Docker {

    private val ubuntu = Ubuntu()

    /**
     * See the [official guide](https://docs.docker.com/engine/install/ubuntu/#install-from-a-package).
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
                "gnupg-agent",
                "software-properties-common"
            ),
            timeout = Duration.ofMinutes(2)
        )
        ssh.execute("curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -")
        ssh.execute(
            """
            sudo add-apt-repository \
               "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
               $(lsb_release -cs) \
               stable"
            """.trimIndent()
        )
        ubuntu.install(
            ssh = ssh,
            packages = listOf("docker-ce", "docker-ce-cli", "containerd.io"),
            timeout = Duration.ofMinutes(5)
        )
        ssh.execute("sudo service docker status || sudo service docker start")
    }
}
