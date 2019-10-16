package com.atlassian.performance.tools.infrastructure.api.jira.flow.server

import com.atlassian.performance.tools.infrastructure.Iostat
import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostInstallFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostStartFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.os.OsMetric
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.api.os.Vmstat
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class LateUbuntuSysstat : PostInstallHook {
    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: PostInstallFlow
    ) {
        val ubuntu = Ubuntu()
        ubuntu.install(ssh, listOf("sysstat"))
        listOf(Vmstat(), Iostat())
            .map { PostStartOsMetric(it) }
            .forEach { flow.hook(it) }
    }
}

internal class PostStartOsMetric(
    private val metric: OsMetric
) : PostStartHook {
    override fun run(ssh: SshConnection, jira: StartedJira, flow: PostStartFlow) {
        val process = metric.start(ssh)
        flow.addReport(RemoteMonitoringProcessReport(process))
    }
}
