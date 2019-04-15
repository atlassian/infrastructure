package com.atlassian.performance.tools.infrastructure.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.StartedJiraHook
import com.atlassian.performance.tools.infrastructure.api.profiler.Profiler
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Bridges the [Profiler] SPI with the [InstalledJiraHook] SPI.
 * In general any [Profiler] can be rewritten as an [InstalledJiraHook] or any other hookPreStart without this bridge.
 */
class ProfilerHook(
    private val profiler: Profiler
) : InstalledJiraHook {
    override fun hook(ssh: SshConnection, jira: InstalledJira, flow: JiraNodeFlow) {
        profiler.install(ssh)
        flow.hookPostStart(InstalledProfiler(profiler))
    }
}

private class InstalledProfiler(
    private val profiler: Profiler
) : StartedJiraHook {

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
