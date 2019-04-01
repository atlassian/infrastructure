package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.Install
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.jvmtasks.api.TaskTimer.time
import com.atlassian.performance.tools.ssh.api.Ssh

class ServerNodeFormula(
    private val name: String,
    private val jiraHomeSource: JiraHomeSource,
    private val install: Install,
    private val productDistribution: ProductDistribution,
    private val ssh: Ssh,
    private val jdk: JavaDevelopmentKit
) {
    fun install(): JiraNode {
        ssh.newConnection().use { shell ->
            val installation = productDistribution.install(shell, ".")
            val home = time("download Jira home") { jiraHomeSource.download(shell) }
            jdk.install(shell)
            val jira = InstalledJira(home, installation, name, jdk)
            val start = install.install(shell, jira)
            return JiraNode(jira, start, name, home, installation, jdk, ssh)
        }
    }
}
