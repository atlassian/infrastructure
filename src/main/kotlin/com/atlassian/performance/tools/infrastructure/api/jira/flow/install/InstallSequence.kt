package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.StartSequence
import com.atlassian.performance.tools.ssh.api.SshConnection

class InstallSequence(
    private val installs: List<Install>
) : Install {
    override fun install(ssh: SshConnection, jira: InstalledJira): Start {
        return StartSequence(installs.map { it.install(ssh, jira) })
    }
}
