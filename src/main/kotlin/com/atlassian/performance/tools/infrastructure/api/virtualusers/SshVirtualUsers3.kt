package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.infrastructure.VirtualUsersJar
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.jvmtasks.api.TaskTimer.time
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import org.apache.logging.log4j.LogManager

class SshVirtualUsers3 private constructor(
    private val name: String,
    private val resultsTransport: ResultsTransport,
    private val jarName: String,
    private val jdk: JavaDevelopmentKit,
    private val ssh: Ssh
) : VirtualUsers {
    private val logger = LogManager.getLogger(this::class.java)

    override fun applyLoad(
        options: VirtualUserOptions
    ) {
        logger.debug("Applying load via $name...")
        ssh.newConnection().use {
            val testingCommand = VirtualUsersJar().testingCommand(
                jdk = jdk,
                jarName = jarName,
                options = options
            )
            it.execute(
                testingCommand,
                options.behavior.load.total + options.behavior.maxOverhead
            )
        }
        logger.debug("$name finished applying load")
    }

    /**
     * Download measurements such as: logs, test metrics using provided [resultsTransport]
     */
    override fun gatherResults() {
        time("gather results from virtual users") {
            val uploadDirectory = "results"
            val resultsDirectory = "$uploadDirectory/virtual-users/$name"

            ssh.newConnection().use { shell ->
                listOf(
                    "mkdir -p $resultsDirectory",
                    "mv test-results $resultsDirectory",
                    "mv diagnoses $resultsDirectory",
                    "mv virtual-users.log $resultsDirectory",
                    "mv virtual-users-out.log $resultsDirectory",
                    "mv virtual-users-error.log $resultsDirectory",
                    "cp /var/log/syslog $resultsDirectory",
                    "cp /var/log/cloud-init.log $resultsDirectory",
                    "cp /var/log/cloud-init-output.log $resultsDirectory",
                    "find $resultsDirectory -empty -type f -delete"
                ).forEach { shell.safeExecute(it) }

                resultsTransport.transportResults(
                    targetDirectory = uploadDirectory,
                    sshConnection = shell
                )
            }
        }
    }

}
