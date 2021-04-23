package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class SharedHomeHook(
    private val jiraHomeSource: JiraHomeSource,
    private val infrastructure: Infrastructure
) : PreInstanceHook {
    private val localHome = "/home/ubuntu/jira-shared-home"

    override fun call(nodes: List<PreInstallHooks>, hooks: PreInstanceHooks, reports: Reports) {
        val server = infrastructure.serveSsh("shared-home")
        server.newConnection().use { ssh ->
            download(ssh)
            export(ssh)
        }
        val sharedHome = RemotePath(server.host, localHome)
        nodes.forEach { it.postInstall.insert(SharedHomeMount(sharedHome)) }
    }

    private fun download(ssh: SshConnection) {
        ssh.execute("sudo mkdir -p $localHome")
        val jiraHome = jiraHomeSource.download(ssh)
        ssh.execute("sudo mv $jiraHome/{data,plugins,import,export} $localHome")
        ssh.safeExecute("sudo mv $jiraHome/logos $localHome")
    }

    private fun export(ssh: SshConnection): SshConnection.SshResult {
        Ubuntu().install(ssh, listOf("nfs-kernel-server"))
        val options = "rw,sync,no_subtree_check,no_root_squash"
        ssh.execute("sudo echo '$localHome ${infrastructure.subnet}($options)' | sudo tee -a /etc/exports")
        return ssh.execute("sudo service nfs-kernel-server restart")
    }

    private class SharedHomeMount(
        private val sharedHome: RemotePath
    ) : PostInstallHook {

        override fun call(ssh: SshConnection, jira: InstalledJira, hooks: PostInstallHooks, reports: Reports) {
            Ubuntu().install(ssh, listOf("nfs-common"))
            val mountSource = "${sharedHome.host.ipAddress}:${sharedHome.path}"
            val mountTarget = "mounted-shared-home"
            ssh.execute("mkdir -p $mountTarget")
            ssh.execute("sudo mount -o soft,intr,rsize=8192,wsize=8192 $mountSource $mountTarget")
            ssh.execute("sudo chown ubuntu:ubuntu $mountTarget")
            val mountedPath = "`realpath $mountTarget`"
            val jiraHome = jira.home.path
            ssh.execute("echo ehcache.object.port = 40011 >> $jiraHome/cluster.properties")
            ssh.execute("echo jira.node.id = ${jira.host.name} >> $jiraHome/cluster.properties")
            ssh.execute("echo jira.shared.home = $mountedPath >> $jiraHome/cluster.properties")
        }
    }
}
