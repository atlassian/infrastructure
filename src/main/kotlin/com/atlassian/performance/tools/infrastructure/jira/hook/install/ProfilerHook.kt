package com.atlassian.performance.tools.infrastructure.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.PostStartHook
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
