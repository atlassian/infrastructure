package com.atlassian.performance.tools.infrastructure.jira.sharedhome

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.jira.instance.ClusterProperties
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class SharedHomeProperty(
    private val jira: InstalledJira
) {

    fun set(mounted: String, ssh: SshConnection) {
        ClusterProperties(jira).set("jira.shared.home", mounted, ssh)
    }
}
