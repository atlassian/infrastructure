package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.infrastructure.VirtualUsersJar
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.api.jvm.OpenJDK11
import com.atlassian.performance.tools.infrastructure.api.os.MonitoringProcess
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.jvmtasks.api.TaskTimer.time
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.util.*

/**
 * [VirtualUsers] running on a remote machine which is managed via [ssh]
 *
 * @param name of the instance, defaults to [UUID.randomUUID]
 * @param nodeOrder of this machine, should start with `0` for the first node and be increased by `1` for each next node
 * @param resultsTransport defining how the test results will be obtained
 * @param jarName of the virtual users executable
 * @param ssh used for communication
 */
class SshVirtualUsers(
    private val name: String = UUID.randomUUID().toString(),
    private val nodeOrder: Int,
    private val resultsTransport: ResultsTransport,
    private val jarName: String,
    val ssh: Ssh
) : VirtualUsers {

    private val logger = LogManager.getLogger(this::class.java)
    private val jdk: JavaDevelopmentKit = OpenJDK11()

    override fun applyLoad(
        options: VirtualUserOptions
    ) {
        logger.debug("Applying load via $name...")
        ssh.newConnection().use {
            Ubuntu().install(it, listOf("curl"))
            jdk.install(it)

            it.safeExecute(
                "curl --head ${options.jiraAddress}",
                Duration.ofSeconds(30),
                Level.DEBUG,
                Level.DEBUG
            )
            Ubuntu().metrics(it)
            val process = PidStat().start(it)
            Thread.sleep(4000)
            process.stop(it)
            val artifact = process.getResultPath()
            it.execute("cat $artifact")
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

    class PidStat : OsMetric {
        private val LOG_PATH: String = "~/jpt-pidstat.log"

        override fun start(connection: SshConnection): RemoteMonitoringProcess {
            return startMonitoring(connection)
        }

        override fun startMonitoring(connection: SshConnection): MonitoringProcess {
            val pidstat = connection.startProcess("pidstat -drsuw -t 2 > $LOG_PATH")
            return MonitoringProcess(pidstat, LOG_PATH)
        }
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

    override fun toString(): String {
        return "SshVirtualUsers(name='$name', nodeOrder=$nodeOrder, resultsTransport=$resultsTransport, jarName='$jarName', ssh=$ssh, logger=$logger, jdk=$jdk)"
    }
}
