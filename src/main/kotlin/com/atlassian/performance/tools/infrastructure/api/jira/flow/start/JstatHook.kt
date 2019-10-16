package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostStartFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JstatHook : PostStartHook {

    override fun run(
        ssh: SshConnection,
        jira: StartedJira,
        flow: PostStartFlow
    ) {
        val process = jira.installed.jdk.jstatMonitoring.start(ssh, jira.pid)
        flow.addReport(RemoteMonitoringProcessReport(process))
    }
}
