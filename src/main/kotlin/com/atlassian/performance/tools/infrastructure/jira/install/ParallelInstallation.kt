package com.atlassian.performance.tools.infrastructure.jira.install

import com.atlassian.performance.tools.concurrency.api.submitWithLogContext
import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.downloadRemotely
import com.atlassian.performance.tools.infrastructure.installRemotely
import java.util.concurrent.Executors

class ParallelInstallation(
    private val jiraHomeSource: JiraHomeSource,
    private val productDistribution: ProductDistribution,
    private val jdk: JavaDevelopmentKit
) : JiraInstallation {

    override fun install(
        server: TcpServer
    ): InstalledJira {
        server.ssh.newConnection().use { ssh ->
            val pool = Executors.newCachedThreadPool { runnable ->
                Thread(runnable, "jira-installation-${runnable.hashCode()}")
            }
            val product = pool.submitWithLogContext("product") {
                productDistribution.installRemotely(ssh, ".")
            }
            val home = pool.submitWithLogContext("home") {
                jiraHomeSource.downloadRemotely(ssh)
            }
            val java = pool.submitWithLogContext("java") {
                jdk.also { it.install(ssh) }
            }
            val jira = InstalledJira(home.get(), product.get(), java.get(), server)
            pool.shutdownNow()
            return jira
        }
    }
}
