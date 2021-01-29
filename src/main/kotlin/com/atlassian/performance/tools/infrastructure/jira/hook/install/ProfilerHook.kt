package com.atlassian.performance.tools.infrastructure.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.profiler.Profiler
import com.atlassian.performance.tools.infrastructure.jira.hook.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Bridges the [Profiler] SPI with the [PostInstallHook] SPI.
 * In general any [Profiler] can be rewritten as an [PostInstallHook] or any other hookPreStart without this bridge.
 */
class ProfilerHook(
    private val profiler: Profiler
) : PostInstallHook {
    override fun call(ssh: SshConnection, jira: InstalledJira, hooks: PostInstallHooks) {
        profiler.install(ssh)
        hooks.preStart.postStart.insert(InstalledProfiler(profiler))
    }
}

private class InstalledProfiler(
    private val profiler: Profiler
) : PostStartHook {

    override fun call(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks
    ) {
        val process = profiler.start(ssh, jira.pid)
        if (process != null) {
            hooks.reports.add(RemoteMonitoringProcessReport(process))
        }
    }
}
