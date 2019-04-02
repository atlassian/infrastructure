package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.Install
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.jvmtasks.api.TaskTimer.time
import com.atlassian.performance.tools.ssh.api.SshConnection

class InstallableJira(
    private val name: String,
    private val jiraHomeSource: JiraHomeSource, // TODO add database too, but how to handle databaseIp?
    private val installHook: Install,
    private val productDistribution: ProductDistribution,
    private val jdk: JavaDevelopmentKit
) {
    fun install(
        ssh: SshConnection,
        server: TcpServer
    ): StartableJira {
        val installation = productDistribution.install(ssh, ".")
        val home = time("download Jira home") { jiraHomeSource.download(ssh) }
        jdk.install(ssh)
        val installed = InstalledJira(home, installation, name, jdk, server)
        val startHook = installHook.install(ssh, installed)
        return StartableJira(installed, startHook)
    }
}
