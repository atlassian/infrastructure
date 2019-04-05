package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.ssh.api.SshConnection

interface PreStartHook {
    fun hook(
        ssh: SshConnection,
        jira: InstalledJira,
        track: ReportTrack
    )
}
