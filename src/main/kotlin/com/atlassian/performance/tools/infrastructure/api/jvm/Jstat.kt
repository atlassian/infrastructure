package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.os.MonitoringProcess
import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class Jstat(
    private val jvmBin: String
) {
    companion object {
        val LOG_FILE_NAME = "jpt-jstat.log"
        private val INTERVAL: Duration = Duration.ofSeconds(2)
        private val LOG_PATH: String = "./$LOG_FILE_NAME"

        private val TIME = "date -Iseconds"
        private val ADD_TIME =
            "while IFS= read -r line; do " +
                "echo \"\$($TIME) \$line\"; " +
                "done"
    }

    @Deprecated("Use `start` method.")
    fun startMonitoring(
        connection: SshConnection,
        pid: String,
        option: String = "-gcutil"
    ): MonitoringProcess {
        val interval = "${INTERVAL.seconds}s"
        val process = connection.startProcess("${jvmBin}jstat $option -t $pid $interval | $ADD_TIME > $LOG_PATH")
        return MonitoringProcess(process, LOG_PATH)
    }

    fun start(
        connection: SshConnection,
        pid: Int
    ): RemoteMonitoringProcess {
        val option = "-gcutil"
        return start(connection, pid, option)
    }

    fun start(
        connection: SshConnection,
        pid: Int,
        option: String
    ): RemoteMonitoringProcess {
        val interval = "${INTERVAL.seconds}s"
        val process = connection.startProcess("${jvmBin}jstat $option -t $pid $interval | $ADD_TIME > $LOG_PATH")
        return MonitoringProcess(process, LOG_PATH)
    }
}
