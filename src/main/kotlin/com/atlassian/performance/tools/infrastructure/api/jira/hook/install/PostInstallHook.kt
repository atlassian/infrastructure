package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostInstallHooks
import com.atlassian.performance.tools.ssh.api.SshConnection

interface PostInstallHook {

    fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    )
}
