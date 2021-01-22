package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.hook.JiraNodeHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jvm.S3HostedJdk
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.infrastructure.ubuntu.EarlyUbuntuSysstat
import com.atlassian.performance.tools.jvmtasks.api.Backoff
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.util.function.Consumer

class HookedJiraStartIT {

    @Test
    fun shouldStartJiraWithDefaultHooks() {
        // given
        val hooks = JiraNodeHooks.default()
        val jiraInstallation = HookedJiraInstallation(
            ParallelInstallation(
                jiraHomeSource = EmptyJiraHome(),
                productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                jdk = S3HostedJdk()
            ),
            hooks.preInstall
        )
        val jiraStart = HookedJiraStart(JiraLaunchScript(), hooks.preStart)
        val privatePort = 8080
        val container = SshUbuntuContainer(Consumer {
            it.addExposedPort(privatePort)
        })
        val remoteReports = container.start().use { sshUbuntu ->
            val server = TcpServer(
                "localhost",
                sshUbuntu.container.getMappedPort(privatePort),
                privatePort,
                "my-jira"
            )
            val remoteReports = sshUbuntu.toSsh().newConnection().use { ssh ->
                // when
                val installed = jiraInstallation.install(ssh, server)
                val started = jiraStart.start(ssh, installed)
                stop(started, ssh)
                hooks.reports.list().flatMap { it.locate(ssh) }
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

    @Test
    fun shouldDownloadPartialReportsInCaseOfFailure() {
        // given
        val hooks = JiraNodeHooks.empty().apply {
            preInstall.insert(EarlyUbuntuSysstat())
            preInstall.postInstall.preStart.postStart.insert(FailingHook())
        }
        val jiraInstallation = HookedJiraInstallation(
            ParallelInstallation(
                jiraHomeSource = EmptyJiraHome(),
                productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                jdk = S3HostedJdk()
            ),
            hooks = hooks.preInstall
        )
        val privatePort = 8080
        val container = SshUbuntuContainer(Consumer {
            it.addExposedPort(privatePort)
        })
        val remoteReports = container.start().use { sshUbuntu ->
            val server = TcpServer(
                "localhost",
                sshUbuntu.container.getMappedPort(privatePort),
                privatePort,
                "my-jira"
            )
            return@use sshUbuntu.toSsh().newConnection().use useSsh@{ ssh ->
                // when
                try {
                    jiraInstallation.install(ssh, server)
                } catch (e: Exception) {
                    println("Failed: ${e.message}")
                }
                return@useSsh hooks.reports.list().flatMap { it.locate(ssh) }
            }
        }

        // then
        assertThat(remoteReports).contains(
            "./jpt-vmstat.log",
            "./jpt-iostat.log"
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

private class FailingHook : PostStartHook {
    override fun call(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks) {
        throw Exception("Expected failure")
    }
}

class StaticBackoff(
    private val backoff: Duration
) : Backoff {

    override fun backOff(attempt: Int): Duration = backoff
}
