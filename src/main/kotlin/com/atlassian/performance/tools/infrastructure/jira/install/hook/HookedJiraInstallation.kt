package com.atlassian.performance.tools.infrastructure.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports

class HookedJiraInstallation(
    private val installation: JiraInstallation,
    private val hooks: PreInstallHooks
) : JiraInstallation {

    override fun install(
        tcp: TcpHost,
        reports: Reports
    ): InstalledJira {
        tcp.ssh.newConnection().use { ssh ->
            hooks.call(ssh, tcp, reports)
        }
        val installed = installation.install(tcp, reports)
        tcp.ssh.newConnection().use { ssh ->
            hooks.postInstall.call(ssh, installed, reports)
        }
        return installed
    }
}
