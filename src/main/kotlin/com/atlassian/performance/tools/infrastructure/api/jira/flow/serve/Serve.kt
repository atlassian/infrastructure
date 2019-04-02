package com.atlassian.performance.tools.infrastructure.api.jira.flow.serve

import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Hooks in before Jira starts serving to clients after [Upgrade].
 */
interface Serve {

    fun serve(
        ssh: SshConnection,
        jira: StartedJira
    ): Report
}
