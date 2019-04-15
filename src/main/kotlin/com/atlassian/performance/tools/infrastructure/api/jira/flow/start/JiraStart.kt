package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.ssh.api.SshConnection

interface JiraStart {

    fun start(
        ssh: SshConnection,
        installed: InstalledJira,
        flow: JiraNodeFlow
    ): StartedJira
}
