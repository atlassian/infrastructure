package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration.ofMinutes

class StartableJira(
    private val installed: InstalledJira,
    private val startHook: Start
) {
    fun start(
        ssh: SshConnection
    ): ServeableJira {
        val upgradeHook = startHook.start(ssh, installed)
        val pid = startJira(ssh)
        val started = StartedJira(installed, pid)
        val serveHook = upgrade(upgradeHook, ssh, started)
        return ServeableJira(started, serveHook)
    }

    private fun upgrade(
        upgrade: Upgrade,
        sshConnection: SshConnection,
        started: StartedJira
    ): Serve {
        try {
            return upgrade.upgrade(sshConnection, started)
        } catch (exception: Exception) {
            TODO("Somehow download partial reports")
            throw Exception("Failed to startHook the Jira node.", exception)
        }
    }

    private fun startJira(
        ssh: SshConnection
    ): Int {
        ssh.execute(
            "${installed.jdk.use()}; ${installed.installation}/bin/start-jira.sh",
            ofMinutes(1)
        )
        return pid(ssh)
    }

    private fun pid(
        ssh: SshConnection
    ): Int = ssh
        .execute("cat ${installed.installation}/work/catalina.pid")
        .output
        .trim()
        .toInt()
}
