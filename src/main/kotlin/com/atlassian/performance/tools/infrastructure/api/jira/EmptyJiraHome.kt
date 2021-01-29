package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Contains no files. Represents a brand new Jira instance.
 *
 * @since 4.18.0
 */
class EmptyJiraHome : JiraHomeSource {
    override fun download(ssh: SshConnection): String {
        val jiraHome = "jira-home"
        ssh.execute("mkdir $jiraHome")
        return jiraHome
    }
}