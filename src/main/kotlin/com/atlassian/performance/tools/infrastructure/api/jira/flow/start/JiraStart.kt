package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection
import net.jcip.annotations.ThreadSafe

@ThreadSafe
interface JiraStart {

    fun start(
        ssh: SshConnection,
        installed: InstalledJira,
        flow: JiraNodeFlow
    ): StartedJira
}
