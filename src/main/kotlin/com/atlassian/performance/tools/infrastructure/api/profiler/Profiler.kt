package com.atlassian.performance.tools.infrastructure.api.profiler

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * SPI for a profiler
 */
interface Profiler {

    /**
     * Installs profiler. You can also use this SPI to configure Jira before startup.
     */
    fun install(ssh: SshConnection)

    /**
     * @return returns a process that can be stopped and provide results. You can use `null` to indicate there's no profiler.
     */
    fun start(
        ssh: SshConnection,
        pid: Int
    ): RemoteMonitoringProcess?
}