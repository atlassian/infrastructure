package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.infrastructure.Iostat
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.JitterBackoff
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.jvmtasks.api.plus
import com.atlassian.performance.tools.ssh.api.SshConnection
import net.jcip.annotations.ThreadSafe
import org.apache.logging.log4j.Level
import java.time.Duration
import java.time.Duration.ofSeconds
import java.util.concurrent.ConcurrentHashMap

@ThreadSafe
class Ubuntu {

    private companion object {
        private val LOCKS = ConcurrentHashMap<String, Any>()
    }

    fun install(
        ssh: SshConnection,
        packages: List<String>,
        timeout: Duration = Duration.ofMinutes(1)
    ) {
        IdempotentAction("install $packages") {
            tryInstalling(
                ssh,
                packages,
                timeout
            )
        }
            .retry(
                maxAttempts = 7, //we need to accommodate cron-based image updates happening in the background
                backoff = StaticBackoff(ofSeconds(10)) + JitterBackoff(ofSeconds(5))
            )
    }

    private fun tryInstalling(
        ssh: SshConnection,
        packages: List<String>,
        timeout: Duration
    ) {
        val joinedPackages = packages.joinToString(separator = " ")
        try {
            updatePackageIndex(ssh)
            ssh.lockApt {
                it.execute(
                    cmd = "sudo DEBIAN_FRONTEND=noninteractive apt-get install -qq $joinedPackages",
                    timeout = timeout,
                    stdout = Level.TRACE,
                    stderr = Level.TRACE
                )
            }
        } catch (e: Exception) {
            cleanInterruptedApt(ssh)
            throw Exception("Failed an attempt to install $packages", e)
        }
    }

    /**
     * Cleans up after a potentially interrupted `apt-get` command.
     */
    private fun cleanInterruptedApt(ssh: SshConnection) {
        ssh.lockApt {
            val pid = ssh.safeExecute("pidof apt-get").output.trim().toIntOrNull()
            if (pid != null) {
                it.safeExecute("kill -9 $pid")
                it.execute("sudo rm -rf /var/lib/apt/lists/*")
                it.execute("sudo rm -rf /var/lib/dpkg/updates/*")
            }
        }
    }

    fun metrics(
        connection: SshConnection
    ): List<OsMetric> {
        install(connection, listOf("sysstat"))
        return listOf(
            Vmstat(),
            Iostat()
        )
    }

    private fun updatePackageIndex(ssh: SshConnection) {
        ssh.lockApt {
            it.execute("sudo apt-get update -qq", Duration.ofMinutes(3))
        }
    }

    fun addKey(
        ssh: SshConnection,
        keyId: String
    ) {
        ssh.lockApt {
            it.execute("sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys $keyId 2>&1 | grep -v 'stdout is not a terminal'")
        }
    }

    /**
     * @since 4.27.0
     */
    fun addRemoteKey(
        ssh: SshConnection,
        keyUrl: String
    ) {
        ssh.lockApt {
            it.execute("wget -q -O - $keyUrl | sudo apt-key add -")
        }
    }

    @Deprecated("Use the version with declared file name instead", ReplaceWith("addRepository()"))
    fun addRepository(
        ssh: SshConnection,
        repository: String
    ) {
        install(ssh, listOf("software-properties-common"))
        ssh.lockApt {
            it.execute("sudo add-apt-repository '$repository'")
        }
    }

    fun addRepository(
        ssh: SshConnection,
        repository: String,
        sourceFileName: String
    ) {
        ssh.lockApt {
            it.execute("echo '$repository' | sudo tee /etc/apt/sources.list.d/${sourceFileName}.list")
        }
        updatePackageIndex(ssh)
    }

    fun getDistributionCodename(ssh: SshConnection): String {
        return ssh.execute(". /etc/lsb-release ; echo \$DISTRIB_CODENAME").output.trimEnd('\n')
    }

    private fun SshConnection.lockApt(lambda: (SshConnection) -> Unit) {
        val host = getHost()
        val lockId = host.ipAddress + host.port
        val lock = LOCKS.computeIfAbsent(lockId) { Object() }
        synchronized(lock) {
            lambda(this)
        }
    }
}
