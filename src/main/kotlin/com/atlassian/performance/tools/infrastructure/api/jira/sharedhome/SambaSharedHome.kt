package com.atlassian.performance.tools.infrastructure.api.jira.sharedhome

import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PostInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PostInstanceHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHook
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.network.TcpServerRoom
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.jira.sharedhome.SharedHomeProperty
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*

class SambaSharedHome(
    private val jiraHomeSource: JiraHomeSource,
    private val serverRoom: TcpServerRoom
) : PreInstanceHook {

    override fun call(nodes: List<PreInstallHooks>, hooks: PostInstanceHooks, reports: Reports) {
        val server = serverRoom.serveTcp("samba-shared-home", listOf(139, 445), listOf(137, 138))
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

    private fun export(ssh: SshConnection, sharedHome: String, server: TcpNode): SambaMount {
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
            val mounted = "`realpath $mountTarget`"
            SharedHomeProperty(jira).set(mounted, ssh)
        }
    }
}
