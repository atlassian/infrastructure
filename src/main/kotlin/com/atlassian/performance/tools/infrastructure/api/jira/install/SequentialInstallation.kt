package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.ssh.api.SshConnection

class SequentialInstallation(
    private val jiraHomeSource: JiraHomeSource,
    private val productDistribution: ProductDistribution,
    private val jdk: JavaDevelopmentKit
) : JiraInstallation {

    override fun install(
        ssh: SshConnection,
        server: TcpServer
    ): InstalledJira {
        val installation = productDistribution.install(ssh, ".")
        val home = jiraHomeSource.download(ssh)
        jdk.install(ssh)
        return InstalledJira(home, installation, jdk, server)
    }
}
