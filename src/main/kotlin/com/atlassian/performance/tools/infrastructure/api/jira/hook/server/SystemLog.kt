package com.atlassian.performance.tools.infrastructure.api.jira.hook.server

import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.StaticReport
import com.atlassian.performance.tools.ssh.api.SshConnection

class SystemLog : Report {

    override fun locate(ssh: SshConnection): List<String> {
        return StaticReport("/var/log/syslog").locate(ssh)
    }
}
