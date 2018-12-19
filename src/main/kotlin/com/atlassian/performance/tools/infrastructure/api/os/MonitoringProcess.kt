package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection

@Deprecated(
    message = "It will not be a part of API in the future MAJOR release. You can implement RemoteMonitoringProcess instead."
)
class MonitoringProcess(
    private val process: DetachedProcess,
    private val logFile: String
) : RemoteMonitoringProcess {

    @Deprecated("Use `stop` method to stop the process.")
    fun getProcess(): DetachedProcess {
        return process
    }

    @Deprecated(
        message = "Use `stop` method to stop the process.",
        replaceWith = ReplaceWith("getResultPath()")
    )
    fun getLogFile(): String {
        return getResultPath()
    }

    override fun toString(): String {
        return "MonitoringProcess(process=$process, logFile='$logFile')"
    }

    override fun stop(ssh: SshConnection) {
        ssh.stopProcess(process)
    }

    override fun getResultPath(): String {
        return logFile
    }
}