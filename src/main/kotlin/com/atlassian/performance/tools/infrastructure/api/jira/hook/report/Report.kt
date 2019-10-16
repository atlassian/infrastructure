package com.atlassian.performance.tools.infrastructure.api.jira.hook.report

import com.atlassian.performance.tools.ssh.api.SshConnection

interface Report {
    fun locate(ssh: SshConnection): List<String>
}
