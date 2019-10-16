package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostInstallHooks
import com.atlassian.performance.tools.ssh.api.SshConnection

class JiraHomeProperty : PostInstallHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    ) {
        val properties = "${jira.installation}/atlassian-jira/WEB-INF/classes/jira-application.properties"
        ssh.execute("echo jira.home=`realpath ${jira.home}` > $properties")
    }
}
