package com.atlassian.performance.tools.infrastructure.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class SplunkForwarderHook(
    private val splunk: SplunkForwarder
) : PostInstallHook {

    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks,
        reports: Reports
    ) {
        splunk.jsonifyLog4j(ssh, "${jira.installation.path}/atlassian-jira/WEB-INF/classes/log4j.properties")
        splunk.run(ssh, jira.host.name, "/home/ubuntu/jirahome/log")
    }
}
