package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.ssh.api.SshConnection

class HookedJiraInstallation(
    private val installation: JiraInstallation
) : JiraInstallation {

    override fun install(
        ssh: SshConnection,
        server: TcpServer,
        flow: JiraNodeFlow
    ): InstalledJira {
        flow.listPreInstallHooks().forEach { it.hook(ssh, server, flow) }
        val installed = installation.install(ssh, server, flow)
        flow.listPostInstallHooks().forEach { it.hook(ssh, installed, flow) }
        return installed
    }
}
