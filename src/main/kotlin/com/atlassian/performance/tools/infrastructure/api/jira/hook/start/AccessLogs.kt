package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class AccessLogs : PostStartHook {

    override fun call(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks) {
        hooks.reports.add(FileListing("${jira.installed.installation}/logs/*access*"))
    }
}
