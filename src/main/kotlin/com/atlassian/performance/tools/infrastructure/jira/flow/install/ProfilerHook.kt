package com.atlassian.performance.tools.infrastructure.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.profiler.Profiler
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Bridges the [Profiler] SPI with the [PostInstallHook] SPI.
 * In general any [Profiler] can be rewritten as an [PostInstallHook] or any other hook without this bridge.
 */
class ProfilerHook(
    private val profiler: Profiler
) : PostInstallHook {
    override fun hook(ssh: SshConnection, jira: InstalledJira, flow: JiraNodeFlow) {
        profiler.install(ssh)
        flow.postStartHooks.add(InstalledProfiler(profiler))
    }
}

private class InstalledProfiler(
    private val profiler: Profiler
) : PostStartHook {

    override fun hook(
        ssh: SshConnection,
        jira: StartedJira,
        flow: JiraNodeFlow
    ) {
        val process = profiler.start(ssh, jira.pid)
        if (process != null) {
            flow.reports.add(RemoteMonitoringProcessReport(process))
        }
    }
}
