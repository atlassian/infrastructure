package com.atlassian.performance.tools.infrastructure.jira.home

import com.atlassian.performance.tools.infrastructure.DatasetPackage
import com.atlassian.performance.tools.ssh.SshConnection

data class JiraHomePackage(
    private val source: DatasetPackage
) : JiraHomeSource {

    override fun download(
        ssh: SshConnection
    ): String {
        return source.download(ssh)
    }
}