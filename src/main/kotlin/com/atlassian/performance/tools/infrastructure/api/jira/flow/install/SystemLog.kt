package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.StaticReport
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PassingStart
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.ssh.api.SshConnection

class SystemLog : Install {
    override fun install(ssh: SshConnection, jira: InstalledJira): Start {
        return PassingStart(StaticReport("/var/log/syslog"))
    }

}
