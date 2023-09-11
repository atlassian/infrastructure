package com.atlassian.performance.tools.infrastructure.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports

class HookedJiraInstallation(
    private val installation: JiraInstallation,
    private val hooks: PreInstallHooks
) : JiraInstallation {

    override fun install(
        http: HttpNode,
        reports: Reports
    ): InstalledJira {
        http.tcp.ssh.newConnection().use { ssh ->
            hooks.call(ssh, http, reports)
        }
        val installed = installation.install(http, reports)
        http.tcp.ssh.newConnection().use { ssh ->
            hooks.postInstall.call(ssh, installed, reports)
        }
        return installed
    }
}
