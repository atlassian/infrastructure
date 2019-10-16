package com.atlassian.performance.tools.infrastructure.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostInstallFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class SplunkForwarderHook(
    private val splunk: SplunkForwarder
) : PostInstallHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: PostInstallFlow
    ) {
        splunk.jsonifyLog4j(ssh, "${jira.installation}/atlassian-jira/WEB-INF/classes/log4j.properties")
        splunk.run(ssh, jira.server.name, "/home/ubuntu/jirahome/log")
    }
}
