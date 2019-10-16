package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.StartedJira
import com.atlassian.performance.tools.infrastructure.jira.hook.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JstatHook : PostStartHook {

    override fun run(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks
    ) {
        val process = jira.installed.jdk.jstatMonitoring.start(ssh, jira.pid)
        hooks.addReport(RemoteMonitoringProcessReport(process))
    }
}
