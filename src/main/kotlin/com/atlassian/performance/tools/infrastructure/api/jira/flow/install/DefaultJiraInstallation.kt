package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultJiraInstallation(
    private val jiraHomeSource: JiraHomeSource, // TODO add database too, but how to handle databaseIp? Perhaps as a wrapper, which could be applied by the "DB installing" code?
    private val productDistribution: ProductDistribution,
    private val jdk: JavaDevelopmentKit
) : JiraInstallation {

    override fun install(
        ssh: SshConnection,
        server: TcpServer,
        flow: JiraNodeFlow
    ): InstalledJira {
        val installation = productDistribution.install(ssh, ".")
        val home = jiraHomeSource.download(ssh)
        jdk.install(ssh)
        return InstalledJira(home, installation, jdk, server)
    }
}
