package com.atlassian.performance.tools.infrastructure.api.jira.flow.report

import com.atlassian.performance.tools.ssh.api.SshConnection

class EmptyReport : Report {
    override fun locate(ssh: SshConnection): List<String> = emptyList()
}
