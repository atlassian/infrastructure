package com.atlassian.performance.tools.infrastructure.os

import com.atlassian.performance.tools.infrastructure.api.os.MonitoringProcess
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class Pidstat private constructor(
    private val logPath: String,
    private val params: String
) : OsMetric {
    override fun startMonitoring(connection: SshConnection): MonitoringProcess {
        throw NotImplementedError("Use `start` method.")
    }

    override fun start(ssh: SshConnection): RemoteMonitoringProcess {
        val process = ssh.startProcess("pidstat $params &> $logPath")
        return MonitoringProcess(process, logPath)
    }

    class Builder {
        private var logPath: String = "~/jpt-pidstat.log"

        /**
        -d: I/O
        -r: memory
        -u: CPU
        -w: task switching
        -h: output easier for parsing
        -l: full command line
        -H: timestamp in seconds since epoch
        -t: every n seconds
         */
        private var params: String = "-druwhlH -t 2"

        fun logPath(logPath: String): Builder = apply { this.logPath = logPath }
        fun params(params: String): Builder = apply { this.params = params }

        fun build(): OsMetric = Pidstat(
            logPath = logPath,
            params = params
        )
    }
}
