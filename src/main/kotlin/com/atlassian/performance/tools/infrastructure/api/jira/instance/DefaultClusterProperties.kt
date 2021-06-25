package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.jira.instance.ClusterProperties
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultClusterProperties : PostInstallHook {

    override fun call(ssh: SshConnection, jira: InstalledJira, hooks: PostInstallHooks, reports: Reports) {
        ClusterProperties(jira).apply {
            set("jira.node.id", jira.http.tcp.name, ssh)
            set("ehcache.object.port", "40011", ssh)
        }
    }
}
