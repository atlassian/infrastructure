package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstallableJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.DefaultInstall
import com.atlassian.performance.tools.infrastructure.api.jvm.OracleJDK
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.function.Consumer

class InstallableJiraIT {

    @Test
    fun shouldFlowThroughAllSteps() {
        val installHook = DefaultInstall(JiraNodeConfig.Builder().build())
        val formula = InstallableJira(
            name = "test",
            jiraHomeSource = EmptyJiraHome(),
            installHook = installHook,
            productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
            jdk = OracleJDK()
        )
        val privatePort = 8080
        val container = SshUbuntuContainer(Consumer {
            it.addExposedPort(privatePort)
        })

        val remoteReports = container.start().use { sshUbuntu ->
            val server = TcpServer(
                "localhost",
                sshUbuntu.container.getMappedPort(privatePort),
                privatePort
            )
            val remoteReports = sshUbuntu.toSsh().newConnection().use { ssh ->
                formula
                    .install(ssh, server)
                    .start(ssh)
                    .serve(ssh)
                    .report(ssh)
            }
            sshUbuntu.toSsh().newConnection().use { ssh ->
                download(remoteReports, ssh)
            }
            return@use remoteReports
        }

        assertThat(remoteReports).contains(
            "~/jpt-vmstat.log",
            "~/jpt-iostat.log",
            "jira-home/log/atlassian-jira.log",
            "./atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
            "~/jpt-jstat.log"
        )
    }

    private fun download(
        remotes: List<String>,
        ssh: SshConnection
    ): List<File> {
        val downloads = Files.createTempDirectory("apt-infra-test")
        return remotes.map { remote ->
            val path = downloads.resolve(remote)
            ssh.download(remote, path)
            return@map path.toFile()
        }
    }
}

private class EmptyJiraHome : JiraHomeSource {
    override fun download(ssh: SshConnection): String {
        val jiraHome = "jira-home"
        ssh.execute("mkdir $jiraHome")
        return jiraHome
    }
}
