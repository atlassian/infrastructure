package com.atlassian.performance.tools.infrastructure.profiler

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.infrastructure.api.profiler.Profiler
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class DisabledProfiler : Profiler {
    override fun install(ssh: SshConnection) {
    }

    override fun start(ssh: SshConnection, pid: Int): RemoteMonitoringProcess? {
        return null
    }
}