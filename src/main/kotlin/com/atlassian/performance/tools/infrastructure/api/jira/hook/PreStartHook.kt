package com.atlassian.performance.tools.infrastructure.api.jira.hook

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection

interface PreStartHook {

    fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PreStartHooks
    )
}
