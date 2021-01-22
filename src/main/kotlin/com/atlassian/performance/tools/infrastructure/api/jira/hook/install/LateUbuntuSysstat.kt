package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.Iostat
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.api.os.Vmstat
import com.atlassian.performance.tools.infrastructure.jira.hook.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class LateUbuntuSysstat : PostInstallHook {
    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    ) {
        val ubuntu = Ubuntu()
        ubuntu.install(ssh, listOf("sysstat"))
        listOf(Vmstat(), Iostat())
            .map { PostStartOsMetric(it) }
            .forEach { hooks.postStart.insert(it) }
    }
}

internal class PostStartOsMetric(
    private val metric: OsMetric
) : PostStartHook {
    override fun call(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks) {
        val process = metric.start(ssh)
        hooks.reports.add(RemoteMonitoringProcessReport(process))
    }
}
