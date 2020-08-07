package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

internal class Docker {

    private val ubuntu = Ubuntu()

    /**
     * See the [official guide](https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository).
     */
    fun install(
        ssh: SshConnection
    ) {
        allowAptToUseHttps(ssh)
        addDockerOfficialGpgKey(ssh)
        setUpTheRepository(ssh)
        installDockerEngine(ssh)
        startDockerIfNecessary(ssh)
    }

    private fun allowAptToUseHttps(ssh: SshConnection) {
        ubuntu.install(
            ssh = ssh,
            packages = listOf("ca-certificates", "curl", "gnupg", "lsb-release"),
            timeout = Duration.ofMinutes(2)
        )
    }

    private fun addDockerOfficialGpgKey(ssh: SshConnection) {
        ssh.execute("sudo mkdir -p /etc/apt/keyrings")
        val gpgFix = "--batch" // avoids `gpg: cannot open '/dev/tty'`, Docker instructions don't have this
        val gpg = "sudo gpg $gpgFix --dearmor -o /etc/apt/keyrings/docker.gpg"
        val addGpg = "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | $gpg"
        if (ssh.safeExecute(addGpg).isSuccessful().not()) { // Docker instructions warn about this possible fail
            ssh.execute("sudo chmod a+r /etc/apt/keyrings/docker.gpg")
            ssh.safeExecute(addGpg)
        }
    }

    private fun setUpTheRepository(ssh: SshConnection) {
        val arch = "arch=$(dpkg --print-architecture)"
        val signed = "signed-by=/etc/apt/keyrings/docker.gpg"
        val release = "$(lsb_release -cs) stable"
        val source = "deb [$arch $signed] https://download.docker.com/linux/ubuntu $release"
        ssh.execute("echo \"$source\" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null")
    }

    private fun installDockerEngine(ssh: SshConnection) {
        ubuntu.install(
            ssh = ssh,
            packages = listOf("docker-ce", "docker-ce-cli", "containerd.io"),
            timeout = Duration.ofMinutes(5)
        )
    }

    /**
     * Sometimes it's already running. We don't want stderr spam in that case.
     */
    private fun startDockerIfNecessary(ssh: SshConnection) {
        ssh.execute("sudo service docker status || sudo service docker start")
    }
}
