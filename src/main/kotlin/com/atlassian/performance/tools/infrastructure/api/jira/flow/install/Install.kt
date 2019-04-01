package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Hooks in after installing Jira.
 * We recommend install dependencies here rather than deferring it to later phases like [Start] or [Serve].
 */
interface Install {

    fun install(
        ssh: SshConnection,
        jira: InstalledJira
    ): Start
}

