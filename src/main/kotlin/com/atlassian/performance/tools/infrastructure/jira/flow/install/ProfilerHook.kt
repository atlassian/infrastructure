package com.atlassian.performance.tools.infrastructure.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostInstallFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostStartFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.profiler.Profiler
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Bridges the [Profiler] SPI with the [PostInstallHook] SPI.
 * In general any [Profiler] can be rewritten as an [PostInstallHook] or any other hookPreStart without this bridge.
 */
class ProfilerHook(
    private val profiler: Profiler
) : PostInstallHook {
    override fun run(ssh: SshConnection, jira: InstalledJira, flow: PostInstallFlow) {
        profiler.install(ssh)
        flow.hook(InstalledProfiler(profiler))
    }
}

private class InstalledProfiler(
    private val profiler: Profiler
) : PostStartHook {

    override fun run(
        ssh: SshConnection,
        jira: StartedJira,
        flow: PostStartFlow
    ) {
        val process = profiler.start(ssh, jira.pid)
        if (process != null) {
            flow.addReport(RemoteMonitoringProcessReport(process))
        }
    }
}
