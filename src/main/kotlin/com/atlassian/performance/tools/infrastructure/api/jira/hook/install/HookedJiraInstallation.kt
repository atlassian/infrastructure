package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.ssh.api.SshConnection

class HookedJiraInstallation(
    private val installation: JiraInstallation,
    private val hooks: PreInstallHooks
) : JiraInstallation {

    override fun install(
        ssh: SshConnection,
        server: TcpServer
    ): InstalledJira {
        hooks.call(ssh, server)
        val installed = installation.install(ssh, server)
        hooks.postInstall.call(ssh, installed)
        return installed
    }
}
