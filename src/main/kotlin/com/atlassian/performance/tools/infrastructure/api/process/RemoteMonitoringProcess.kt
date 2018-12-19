package com.atlassian.performance.tools.infrastructure.api.process

import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Represents running process that monitors tested application.
 */
interface RemoteMonitoringProcess {
    /**
     * Stops the process and gathers results.
     */
    fun stop(ssh: SshConnection)

    /**
     * @return path to the monitoring process results.
     */
    fun getResultPath(): String
}