package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
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