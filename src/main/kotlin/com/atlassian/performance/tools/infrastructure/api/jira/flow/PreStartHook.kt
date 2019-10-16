package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection

interface PreStartHook {

    fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: PreStartFlow
    )

}
