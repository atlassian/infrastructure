package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.ssh.api.SshConnection

class EmptyJiraHome : JiraHomeSource {
    override fun download(ssh: SshConnection): String {
        val jiraHome = "jira-home"
        ssh.execute("mkdir $jiraHome")
        return jiraHome
    }
}
