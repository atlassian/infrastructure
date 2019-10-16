package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostInstallFlow
import com.atlassian.performance.tools.ssh.api.SshConnection

interface PostInstallHook {

    fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: PostInstallFlow
    )

}
