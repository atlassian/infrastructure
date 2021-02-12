package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.downloadRemotely
import com.atlassian.performance.tools.infrastructure.installRemotely

class SequentialInstallation(
    private val jiraHomeSource: JiraHomeSource,
    private val productDistribution: ProductDistribution,
    private val jdk: JavaDevelopmentKit
) : JiraInstallation {

    override fun install(
        server: TcpServer
    ): InstalledJira {
        server.ssh.newConnection().use { ssh ->
            val installation = productDistribution.installRemotely(ssh, ".")
            val home = jiraHomeSource.downloadRemotely(ssh)
            jdk.install(ssh)
            return InstalledJira(home, installation, jdk, server)
        }
    }
}
