package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostInstallHooks
import com.atlassian.performance.tools.ssh.api.SshConnection

class DisabledAutoBackup : PostInstallHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    ) {
        ssh.execute("echo jira.autoexport=false > ${jira.home}/jira-config.properties")
    }
}
