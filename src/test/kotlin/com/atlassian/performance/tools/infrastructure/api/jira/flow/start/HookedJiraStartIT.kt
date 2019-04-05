package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.DefaultJiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.DefaultPostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.ReportTrack
import com.atlassian.performance.tools.infrastructure.api.jvm.OracleJDK
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.function.Consumer

class HookedJiraStartIT {

    @Test
    fun shouldStartJiraWithDefaultHooks() {
        // given
        val config = JiraNodeConfig.Builder().build()
        val track = ReportTrack()
        track.postStartHooks.add(DefaultPostStartHook())
        track.postInstallHooks.add(DefaultPostInstallHook(config))
        val jiraInstallation = HookedJiraInstallation(DefaultJiraInstallation(
            jiraHomeSource = EmptyJiraHome(),
            productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
            jdk = OracleJDK()
        ))
        val jiraStart = HookedJiraStart(JiraLaunchScript())
        val privatePort = 8080
        val container = SshUbuntuContainer(Consumer {
            it.addExposedPort(privatePort)
        })

        // when
        val remoteReports = container.start().use { sshUbuntu ->
            val server = TcpServer(
                "localhost",
                sshUbuntu.container.getMappedPort(privatePort),
                privatePort,
                "my-jira"
            )
            val remoteReports = sshUbuntu.toSsh().newConnection().use { ssh ->
                val installed = jiraInstallation.install(ssh, server, track)
                val started = jiraStart.start(ssh, installed, track)
                stop(started, ssh)
                track.reports.flatMap { it.locate(ssh) }
            }

            // then
            sshUbuntu.toSsh().newConnection().use { ssh ->
                download(remoteReports, ssh)
            }
            return@use remoteReports
        }

        assertThat(remoteReports).contains(
            "./jpt-vmstat.log",
            "./jpt-iostat.log",
            "jira-home/log/atlassian-jira.log",
            "./atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
            "./jpt-jstat.log"
        )
    }

    private fun stop(
        started: StartedJira,
        ssh: SshConnection
    ) {
        val installed = started.installed
        ssh.execute("${installed.jdk.use()}; ${installed.installation}/bin/stop-jira.sh")
    }

    private fun download(
        remotes: List<String>,
        ssh: SshConnection
    ): List<File> {
        val downloads = Files.createTempDirectory("apt-infra-test")
        return remotes.map { remote ->
            val local = downloads.resolve("./$remote")
            ssh.download(remote, local)
            return@map local.toFile()
        }
    }
}
