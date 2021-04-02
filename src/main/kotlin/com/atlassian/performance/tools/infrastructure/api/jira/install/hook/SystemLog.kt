package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.ssh.api.SshConnection

class SystemLog : PreInstallHook {

    override fun call(ssh: SshConnection, host: TcpHost, hooks: PreInstallHooks, reports: Reports) {
        reports.add(FileListing("/var/log/syslog"), host)
    }
}
