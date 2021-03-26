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
        host: TcpHost,
        reports: Reports
    ): InstalledJira {
        host.ssh.newConnection().use { ssh ->
            hooks.call(ssh, host, reports)
        }
        val installed = installation.install(host, reports)
        host.ssh.newConnection().use { ssh ->
            hooks.postInstall.call(ssh, installed, reports)
        }
        return installed
    }
}
