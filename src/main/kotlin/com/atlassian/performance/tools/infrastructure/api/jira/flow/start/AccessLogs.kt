package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.FileListing
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.ssh.api.SshConnection

class AccessLogs : PostStartHook {

    override fun hook(ssh: SshConnection, jira: StartedJira, track: ReportTrack) {
        track.reports.add(FileListing("${jira.installed.installation}/logs/*access*"))
    }
}
