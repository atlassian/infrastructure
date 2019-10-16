package com.atlassian.performance.tools.infrastructure.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class SplunkForwarderHook(
    private val splunk: SplunkForwarder
) : PostInstallHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    ) {
        splunk.jsonifyLog4j(ssh, "${jira.installation}/atlassian-jira/WEB-INF/classes/log4j.properties")
        splunk.run(ssh, jira.server.name, "/home/ubuntu/jirahome/log")
    }
}
