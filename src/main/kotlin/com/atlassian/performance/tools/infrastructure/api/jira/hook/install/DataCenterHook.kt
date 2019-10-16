package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.SharedHome
import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostInstallHooks
import com.atlassian.performance.tools.ssh.api.SshConnection

class DataCenterHook(
    private val nodeId: String,
    private val sharedHome: SharedHome
) : PostInstallHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    ) {
        val localSharedHome = sharedHome.localSharedHome
        sharedHome.mount(ssh)
        val jiraHome = jira.home  // TODO what's the difference between localSharedHome and jiraHome? should both be hookable?
        ssh.execute("echo ehcache.object.port = 40011 >> $jiraHome/cluster.properties")
        ssh.execute("echo jira.node.id = $nodeId >> $jiraHome/cluster.properties")
        ssh.execute("echo jira.shared.home = `realpath $localSharedHome` >> $jiraHome/cluster.properties")
    }
}
