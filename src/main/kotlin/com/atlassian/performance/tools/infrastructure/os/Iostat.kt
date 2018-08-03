package com.atlassian.performance.tools.infrastructure.os

import com.atlassian.performance.tools.ssh.SshConnection
import java.time.Duration
import java.time.temporal.ChronoUnit

class Iostat : OsMetric {
    companion object {
        private val DELAY: Duration = Duration.ofSeconds(2)
        private val LOG_PATH: String = "~/jpt-iostat.log"

        private val TIME = "date -u \"+%d-%m-%Y %H:%M:%S UTC\""
        private val ADD_TIME =
            "while IFS= read -r line; do " +
                "echo \"\$($TIME) \$line\"; " +
            "done"
    }

    override fun startMonitoring(
        connection: SshConnection
    ): MonitoringProcess {
        val seconds = DELAY.get(ChronoUnit.SECONDS)
        val process = connection.startProcess("iostat -d $seconds -x | $ADD_TIME > $LOG_PATH")
        return MonitoringProcess(process, LOG_PATH)
    }
}