package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Duration.ofMinutes

class JiraNode(
    private val installed: InstalledJira,
    private val start: Start,
    private val name: String,
    private val analyticLogs: String,
    private val installation: String,
    private val jdk: JavaDevelopmentKit,
    private val ssh: Ssh
) {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    fun start(): Serve {
        val serve = ssh.newConnection().use { shell ->
            logger.info("Starting $name ...")
            val upgrade = start.start(shell, installed)
            startJira(shell)
            val pid = pid(shell)
            val started = StartedJira(installed, pid)
            logger.info("Upgrading $name ...")
            return@use upgrade(upgrade, shell, started)
        }
        logger.info("$name is ready to serve")
        return serve
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
            throw Exception("Failed to start the Jira node.", exception)
        }
    }

    private fun startJira(
        ssh: SshConnection
    ) {
        ssh.execute(
            "${jdk.use()}; ./$installation/bin/start-installed.sh",
            ofMinutes(1)
        )
    }

    private fun pid(
        ssh: SshConnection
    ): Int {
        return ssh
            .execute("cat $installation/work/catalina.pid")
            .output
            .trim()
            .toInt()
    }
}
