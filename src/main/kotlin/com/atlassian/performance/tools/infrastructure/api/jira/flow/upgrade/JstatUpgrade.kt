package com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade

import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.PassingServe
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.infrastructure.jira.flow.RemoteMonitoringProcessReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class JstatUpgrade : Upgrade {

    override fun upgrade(
        ssh: SshConnection,
        jira: StartedJira
    ): Serve {
        val process = jira.installed.jdk.jstatMonitoring.start(ssh, jira.pid)
        return PassingServe(RemoteMonitoringProcessReport(process))
    }
}
