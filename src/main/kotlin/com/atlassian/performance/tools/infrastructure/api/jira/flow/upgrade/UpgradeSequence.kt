package com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade

import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.ServeSequence
import com.atlassian.performance.tools.ssh.api.SshConnection

class UpgradeSequence(
    private val upgrades: List<Upgrade>
) : Upgrade {
    override fun upgrade(ssh: SshConnection, jira: StartedJira): Serve {
        return ServeSequence(upgrades.map { it.upgrade(ssh, jira) })
    }
}
