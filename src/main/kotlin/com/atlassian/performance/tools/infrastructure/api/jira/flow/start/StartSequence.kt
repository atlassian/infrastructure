package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.UpgradeSequence
import com.atlassian.performance.tools.ssh.api.SshConnection

class StartSequence(
    private val starts: List<Start>
) : Start {
    override fun start(ssh: SshConnection, jira: InstalledJira): Upgrade {
        return UpgradeSequence(starts.map { it.start(ssh, jira) })
    }
}
