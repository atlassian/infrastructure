package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.ssh.api.SshConnection

class PostInstallHookSequence(
    private val hooks: List<PostInstallHook>
) : PostInstallHook {

    override fun hook(
        ssh: SshConnection,
        jira: InstalledJira,
        track: ReportTrack
    ) {
        hooks.forEach { it.hook(ssh, jira, track) }
    }
}
