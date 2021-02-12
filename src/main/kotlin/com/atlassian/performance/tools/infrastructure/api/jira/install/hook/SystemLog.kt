package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.report.Report
import com.atlassian.performance.tools.ssh.api.SshConnection

class SystemLog : Report {

    override fun locate(ssh: SshConnection): List<String> {
        return listOf("/var/log/syslog")
    }
}
