package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.ssh.api.SshConnection

interface OsMetric {

    @Deprecated("Use `start` method.")
    fun startMonitoring(
        connection: SshConnection
    ): MonitoringProcess

    fun start(
        connection: SshConnection
    ): RemoteMonitoringProcess
}