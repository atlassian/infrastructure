package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.jira.report.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JstatHook : PostStartHook {

    override fun call(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks
    ) {
        val process = jira.installed.jdk.jstatMonitoring.start(ssh, jira.pid)
        hooks.reports.add(RemoteMonitoringProcessReport(process))
    }
}
