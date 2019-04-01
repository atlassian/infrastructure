package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Hooks in before Jira starts after [Install].
 */
interface Start {

    fun start(
        ssh: SshConnection,
        jira: InstalledJira
    ): Upgrade
}