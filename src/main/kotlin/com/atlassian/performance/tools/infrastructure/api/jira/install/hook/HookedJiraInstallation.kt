package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpServer

class HookedJiraInstallation(
    private val installation: JiraInstallation,
    private val hooks: PreInstallHooks
) : JiraInstallation {

    override fun install(
        server: TcpServer
    ): InstalledJira {
        server.ssh.newConnection().use { ssh ->
            hooks.call(ssh, server)
        }
        val installed = installation.install(server)
        server.ssh.newConnection().use { ssh ->
            hooks.postInstall.call(ssh, installed)
        }
        return installed
    }
}
