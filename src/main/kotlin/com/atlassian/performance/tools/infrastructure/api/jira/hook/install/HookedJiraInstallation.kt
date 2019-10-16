package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.JiraNodeHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.TcpServer
import com.atlassian.performance.tools.ssh.api.SshConnection

class HookedJiraInstallation(
    private val installation: JiraInstallation
) : JiraInstallation {

    override fun install(
        ssh: SshConnection,
        server: TcpServer,
        hooks: JiraNodeHooks
    ): InstalledJira {
        hooks.runPreInstallHooks(ssh, server)
        val installed = installation.install(ssh, server, hooks)
        hooks.runPostInstallHooks(ssh, installed)
        return installed
    }
}
