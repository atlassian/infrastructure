package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost

class HookedJiraInstallation(
    private val installation: JiraInstallation,
    private val hooks: PreInstallHooks
) : JiraInstallation {

    override fun install(
        host: TcpHost
    ): InstalledJira {
        host.ssh.newConnection().use { ssh ->
            hooks.call(ssh, host)
        }
        val installed = installation.install(host)
        host.ssh.newConnection().use { ssh ->
            hooks.postInstall.call(ssh, installed)
        }
        return installed
    }
}
