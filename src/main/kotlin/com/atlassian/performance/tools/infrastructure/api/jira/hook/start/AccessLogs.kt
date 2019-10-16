package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class AccessLogs : PostStartHook {

    override fun run(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks) {
        hooks.addReport(FileListing("${jira.installed.installation}/logs/*access*"))
    }
}
