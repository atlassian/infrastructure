package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostStartFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class AccessLogs : PostStartHook {

    override fun run(ssh: SshConnection, jira: StartedJira, flow: PostStartFlow) {
        flow.addReport(FileListing("${jira.installed.installation}/logs/*access*"))
    }
}
