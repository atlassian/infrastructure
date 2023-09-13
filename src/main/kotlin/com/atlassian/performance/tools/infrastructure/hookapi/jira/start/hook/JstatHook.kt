package com.atlassian.performance.tools.infrastructure.hookapi.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.jira.report.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JstatHook : PostStartHook {

    override fun call(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks,
        reports: Reports
    ) {
        val process = jira.installed.jdk.jstatMonitoring.start(ssh, jira.pid)
        reports.add(RemoteMonitoringProcessReport(process), jira)
    }
}
