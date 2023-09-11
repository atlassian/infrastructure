package com.atlassian.performance.tools.infrastructure.hookapi.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.ssh.api.SshConnection

class AccessLogs : PreStartHook {

    override fun call(ssh: SshConnection, jira: InstalledJira, hooks: PreStartHooks, reports: Reports) {
        reports.add(FileListing("${jira.installation.path}/logs/*access*"), jira)
    }
}
