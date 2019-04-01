package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.EmptyReport
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.PassingServe
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PassingStart
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.infrastructure.api.profiler.Profiler
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class ProfilerInstall(
    private val profiler: Profiler
) : Install {
    override fun install(
        ssh: SshConnection,
        jira: InstalledJira
    ): Start {
        profiler.install(ssh)
        return PassingStart(ProfilerUpgrade(profiler))
    }
}

private class ProfilerUpgrade(
    private val profiler: Profiler
) : Upgrade {

    override fun upgrade(
        ssh: SshConnection,
        jira: StartedJira
    ): Serve {
        val process = profiler.start(ssh, jira.pid)
        val report = when (process) {
            null -> EmptyReport()
            else -> RemoteMonitoringProcessReport(process)
        }
        return PassingServe(report)
    }
}
