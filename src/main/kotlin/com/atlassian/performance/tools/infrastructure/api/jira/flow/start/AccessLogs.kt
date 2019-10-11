package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection

class AccessLogs : StartedJiraHook {

    override fun run(ssh: SshConnection, jira: StartedJira, flow: JiraNodeFlow) {
        flow.reports.add(FileListing("${jira.installed.installation}/logs/*access*"))
    }
}
