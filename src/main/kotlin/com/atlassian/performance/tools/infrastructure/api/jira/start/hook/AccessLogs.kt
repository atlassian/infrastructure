package com.atlassian.performance.tools.infrastructure.api.jira.start.hook

import com.atlassian.performance.tools.infrastructure.api.jira.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class AccessLogs : PostStartHook {

    override fun call(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks) {
        hooks.reports.add(FileListing("${jira.installed.installation}/logs/*access*"))
    }
}
