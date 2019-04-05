package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.ssh.api.SshConnection

class HookedJiraInstallation(
    private val installation: JiraInstallation
) : JiraInstallation {

    override fun install(
        ssh: SshConnection,
        server: TcpServer,
        track: ReportTrack
    ): InstalledJira {
        track.preInstallHooks.forEach { it.hook(ssh, server, track) }
        val installed = installation.install(ssh, server, track)
        track.postInstallHooks.forEach { it.hook(ssh, installed, track) }
        return installed
    }
}
