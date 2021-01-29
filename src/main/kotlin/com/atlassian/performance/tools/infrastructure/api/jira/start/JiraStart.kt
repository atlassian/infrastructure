package com.atlassian.performance.tools.infrastructure.api.jira.start

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection
import net.jcip.annotations.ThreadSafe

@ThreadSafe
interface JiraStart {

    fun start(
        ssh: SshConnection,
        installed: InstalledJira
    ): StartedJira
}
