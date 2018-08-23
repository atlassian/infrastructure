package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.ssh.SshConnection

interface JiraHomeSource {

    /**
     * @return remotely downloaded path
     */
    fun download(ssh: SshConnection): String
}