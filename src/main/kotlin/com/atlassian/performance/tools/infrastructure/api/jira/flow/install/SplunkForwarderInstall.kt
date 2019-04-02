package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.EmptyReport
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PassingStart
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder
import com.atlassian.performance.tools.ssh.api.SshConnection

class SplunkForwarderInstall(
    private val splunk: SplunkForwarder
) : Install {
    override fun install(
        ssh: SshConnection,
        jira: InstalledJira
    ): Start {
        splunk.jsonifyLog4j(ssh, "${jira.installation}/atlassian-jira/WEB-INF/classes/log4j.properties")
        splunk.run(ssh, jira.name, "/home/ubuntu/jirahome/log")
        return PassingStart(EmptyReport())
    }
}
