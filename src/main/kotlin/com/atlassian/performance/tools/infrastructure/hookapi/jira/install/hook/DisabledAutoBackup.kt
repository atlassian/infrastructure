package com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.ssh.api.SshConnection

class DisabledAutoBackup : PostInstallHook {

    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks,
        reports: Reports
    ) {
        ssh.execute("echo jira.autoexport=false > ${jira.home.path}/jira-config.properties")
    }
}
