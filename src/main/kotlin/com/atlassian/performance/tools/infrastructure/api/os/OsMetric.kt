package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.ssh.SshConnection

interface OsMetric {

    fun startMonitoring(
        connection: SshConnection
    ): MonitoringProcess
}