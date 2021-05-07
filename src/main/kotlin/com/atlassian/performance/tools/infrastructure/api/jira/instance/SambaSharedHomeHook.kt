package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*

internal class SambaSharedHomeHook(
    private val jiraHomeSource: JiraHomeSource,
    private val infrastructure: Infrastructure
) : PreInstanceHook {

    override fun call(nodes: List<PreInstallHooks>, hooks: PreInstanceHooks, reports: Reports) {
        val server = infrastructure.serve("samba-shared-home", listOf(139, 445), listOf(137, 138))
        val mount = server.ssh.newConnection().use { ssh ->
            val sharedHome = download(ssh)
            export(ssh, sharedHome, server)
        }
        nodes.forEach { it.postInstall.insert(mount) }
    }

    private fun download(ssh: SshConnection): String {
        val sharedHome = "/home/ubuntu/jira-shared-home"
        ssh.execute("sudo mkdir -p $sharedHome")
        val jiraHome = jiraHomeSource.download(ssh)
        ssh.execute("sudo mv $jiraHome/{data,plugins,import,export} $sharedHome")
        ssh.safeExecute("sudo mv $jiraHome/logos $sharedHome")
        return sharedHome
    }

    private fun export(ssh: SshConnection, sharedHome: String, server: TcpHost): SambaMount {
        Ubuntu().install(ssh, listOf("samba"))
        val shareName = "samba-jira-home"
        val share = """
            [$shareName]
                comment = shared Jira home
                path = $sharedHome
                read only = no
                browsable = no
        """.trimIndent()
        ssh.execute("echo '$share' | sudo tee -a /etc/samba/smb.conf")
        val user = ssh.execute("whoami").output.trim()
        val password = generatePassword()
        // could transfer password via file, but it's an ephemeral secret anyway
        ssh.execute("echo -e '$password\\n$password\\n' | sudo smbpasswd -s -a $user")
        ssh.execute("sudo service smbd restart")
        return SambaMount(server.privateIp, shareName, user, password)
    }

    private fun generatePassword(): String {
        val rng = Random()
        val chars = ('a'..'Z') + ('A'..'Z') + ('0'..'9')
        return (1..32).map { chars[rng.nextInt(chars.size)] }.joinToString("")
    }

    private class SambaMount(
        private val ip: String,
        private val shareName: String,
        private val user: String,
        private val password: String
    ) : PostInstallHook {

        override fun call(ssh: SshConnection, jira: InstalledJira, hooks: PostInstallHooks, reports: Reports) {
            Ubuntu().install(ssh, listOf("cifs-utils"))
            val credentials = "username=$user,password=$password"
            val mountSource = "//$ip/$shareName"
            val mountTarget = "mounted-shared-home"
            ssh.execute("mkdir -p $mountTarget")
            val localUser = ssh.execute("whoami").output.trim()
            ssh.execute("sudo chown $localUser:$localUser $mountTarget")
            ssh.execute("sudo mount -t cifs -o $credentials $mountSource $mountTarget")
            val sharedHome = "`realpath $mountTarget`"
            val nodeHome = jira.home.path
            ssh.execute("echo ehcache.object.port = 40011 >> $nodeHome/cluster.properties")
            ssh.execute("echo jira.node.id = ${jira.host.name} >> $nodeHome/cluster.properties")
            ssh.execute("echo jira.shared.home = $sharedHome >> $nodeHome/cluster.properties")
        }
    }
}
