package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration
import java.time.temporal.ChronoUnit

class Vmstat : OsMetric {

    companion object {
        val LOG_FILE_NAME = "jpt-vmstat.log"
        private val LOG_PATH: String = "~/$LOG_FILE_NAME"
        private val DELAY: Duration = Duration.ofSeconds(2)
    }

    override fun startMonitoring(connection: SshConnection): MonitoringProcess {
        val delayInSeconds = DELAY.get(ChronoUnit.SECONDS)
        val process = connection.startProcess("vmstat -t $delayInSeconds > $LOG_PATH")
        return MonitoringProcess(process, LOG_PATH)
    }

    override fun start(connection: SshConnection): RemoteMonitoringProcess {
        val delayInSeconds = DELAY.get(ChronoUnit.SECONDS)
        val process = connection.startProcess("vmstat -t $delayInSeconds > $LOG_PATH")
        return MonitoringProcess(process, LOG_PATH)
    }
}
