package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.infrastructure.VirtualUsersJar
import com.atlassian.performance.tools.infrastructure.api.jvm.OpenJDK11
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.api.os.Vmstat
import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.infrastructure.os.Iostat
import com.atlassian.performance.tools.infrastructure.os.Pidstat
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
    private val monitoringProcesses = mutableListOf<RemoteMonitoringProcess>()

    override fun applyLoad(options: VirtualUserOptions) {
        logger.debug("Applying load via $name...")
        ssh.newConnection().use { ssh ->
            Ubuntu().install(ssh, listOf("curl"))
            ssh.safeExecute(
                "curl --head ${options.jiraAddress}",
                Duration.ofSeconds(30),
                Level.DEBUG,
                Level.DEBUG
            )

            startCollectingMetrics(ssh)
            applyLoad(ssh, options)
            stopCollectingMetrics(ssh)
        }
        logger.debug("$name finished applying load")
    }

    private fun applyLoad(ssh: SshConnection, options: VirtualUserOptions) {
        val jdk = OpenJDK11()
        jdk.install(ssh)
        val testingCommand = VirtualUsersJar().testingCommand(
            jdk = jdk,
            jarName = jarName,
            options = options
        )
        ssh.execute(
            testingCommand,
            options.behavior.load.total + options.behavior.maxOverhead
        )
    }

    private fun startCollectingMetrics(ssh: SshConnection) {
        Ubuntu().metrics(ssh)
        listOf(Iostat(), Vmstat(), Pidstat.Builder().build()).forEach { metric ->
            monitoringProcesses.add(metric.start(ssh))
        }
    }

    private fun stopCollectingMetrics(ssh: SshConnection) {
        monitoringProcesses.forEach { it.stop(ssh) }
    }

    /**
     * Download measurements such as: logs, test metrics using provided [resultsTransport]
     */
    override fun gatherResults() {
        time("gather results from virtual users") {
            val uploadDirectory = "results"
            val resultsDirectory = "$uploadDirectory/virtual-users/$name"

            ssh.newConnection().use { ssh ->
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
                )
                    .plus(monitoringProcesses.map { it.getResultPath() }.map { "mv $it $resultsDirectory" })
                    .forEach { ssh.safeExecute(it) }

                resultsTransport.transportResults(
                    targetDirectory = uploadDirectory,
                    sshConnection = ssh
                )
            }
        }
    }

    override fun toString(): String {
        return "SshVirtualUsers(name='$name', nodeOrder=$nodeOrder, resultsTransport=$resultsTransport, jarName='$jarName', ssh=$ssh)"
    }
}
