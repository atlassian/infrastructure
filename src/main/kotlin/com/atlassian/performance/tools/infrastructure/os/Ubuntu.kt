package com.atlassian.performance.tools.infrastructure.os

import com.atlassian.performance.tools.jvmtasks.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.IdempotentAction
import com.atlassian.performance.tools.ssh.SshConnection
import org.apache.logging.log4j.Level
import java.time.Duration

class Ubuntu {

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
                maxAttempts = 2,
                backoff = ExponentialBackoff(
                    baseBackoff = Duration.ofSeconds(5)
                )
            )
    }

    private fun tryInstalling(
        ssh: SshConnection,
        packages: List<String>,
        timeout: Duration
    ) {
        val joinedPackages = packages.joinToString(separator = " ")
        ssh.execute("sudo apt-get update -qq", Duration.ofMinutes(1))
        ssh.execute(
            cmd = "sudo DEBIAN_FRONTEND=noninteractive apt-get install -qq $joinedPackages",
            timeout = timeout,
            stdout = Level.TRACE,
            stderr = Level.TRACE
        )
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
}