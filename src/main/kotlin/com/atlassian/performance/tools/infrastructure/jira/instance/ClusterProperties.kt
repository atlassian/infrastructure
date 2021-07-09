package com.atlassian.performance.tools.infrastructure.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class ClusterProperties(
    private val jira: InstalledJira
) {

    fun set(key: String, value: String, ssh: SshConnection) {
        ssh.execute("echo '$key = $value' >> ${jira.home.path}/cluster.properties")
    }
}
