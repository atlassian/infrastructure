package com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.ssh.api.SshConnection

class JiraHomeProperty : PostInstallHook {

    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks,
        reports: Reports
    ) {
        val properties = "${jira.installation.path}/atlassian-jira/WEB-INF/classes/jira-application.properties"
        ssh.execute("echo jira.home=`realpath ${jira.home.path}` > $properties")
    }
}
