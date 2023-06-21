package com.atlassian.performance.tools.infrastructure.api.profiler

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * It can be used to wrap [AsyncProfiler] to avoid errors when the profiler fails to stop.
 */
class FailSafeProfiler(
    private val profiler: Profiler
) : Profiler {
    override fun install(ssh: SshConnection) {
        profiler.install(ssh)
    }

    override fun start(
        ssh: SshConnection,
        pid: Int
    ): RemoteMonitoringProcess? {
        return profiler.start(ssh, pid)?.let { FailSafeProfilerProcess(it) }
    }

    private class FailSafeProfilerProcess(
        private val profilerProcess: RemoteMonitoringProcess
    ) : RemoteMonitoringProcess {
        private val logger: Logger = LogManager.getLogger(this::class.java)

        override fun getResultPath(): String {
            return profilerProcess.getResultPath()
        }

        override fun stop(ssh: SshConnection) {
            try {
                profilerProcess.stop(ssh)
            } catch (e: Exception) {
                logger.warn("Failed to stop profiler process", e)
            }
        }
    }
}

