package com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Hooks into the upgrade process after [Start].
 */
interface Upgrade {

    fun upgrade(
        ssh: SshConnection,
        jira: StartedJira
    ): Serve
}

class StartedJira(
    val installed: InstalledJira,
    val pid: Int
)