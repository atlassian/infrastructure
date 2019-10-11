package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.ssh.api.SshConnection

class JiraHomeProperty : InstalledJiraHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: JiraNodeFlow
    ) {
        val properties = "${jira.installation}/atlassian-jira/WEB-INF/classes/jira-application.properties"
        ssh.execute("echo jira.home=`realpath ${jira.home}` > $properties")
    }
}
