package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.flow.JiraNodeFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.*
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.TcpServerHook
import com.atlassian.performance.tools.infrastructure.api.jvm.Jstat
import com.atlassian.performance.tools.infrastructure.api.jvm.VersionedJavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.infrastructure.ubuntu.EarlyUbuntuSysstat
import com.atlassian.performance.tools.jvmtasks.api.Backoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.time.Duration
import java.util.function.Consumer

class HookedJiraStartIT {

    @Test
    @Ignore
    fun shouldStartJiraWithDefaultHooks() {
        // given
        val config = JiraNodeConfig.Builder().build()
        val flow = JiraNodeFlow()
        flow.hookPostStart(DefaultStartedJiraHook())
        flow.hookPostInstall(DefaultPostInstallHook(config))
        val jiraInstallation = HookedJiraInstallation(ParallelInstallation(
            jiraHomeSource = EmptyJiraHome(),
            productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
            jdk = S3HostedJdk()
        ))
        val jiraStart = HookedJiraStart(JiraLaunchScript())
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
                val installed = jiraInstallation.install(ssh, server, flow)
                val started = jiraStart.start(ssh, installed, flow)
                stop(started, ssh)
                flow.reports.flatMap { it.locate(ssh) }
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
    @Ignore
    fun shouldDownloadPartialReportsInCaseOfFailure() {
        // given
        val flow = JiraNodeFlow()
        flow.hookPreInstall(EarlyUbuntuSysstat())
        flow.hookPostInstall(FailingHook())
        val jiraInstallation = HookedJiraInstallation(ParallelInstallation(
            jiraHomeSource = EmptyJiraHome(),
            productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
            jdk = S3HostedJdk()
        ))
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
                    jiraInstallation.install(ssh, server, flow)
                } catch (e: Exception) {
                    println("Failed: ${e.message}")
                }
                return@useSsh flow.reports.flatMap { it.locate(ssh) }
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

private class FailingHook : TcpServerHook, InstalledJiraHook {
    override fun run(ssh: SshConnection, server: TcpServer, flow: JiraNodeFlow) = throw Exception("Expected failure")
    override fun run(ssh: SshConnection, jira: InstalledJira, flow: JiraNodeFlow) = throw Exception("Expected failure")
}

/**
 * Harvested from https://stash.atlassian.com/projects/JIRASERVER/repos/jira-performance-tests/pull-requests/630
 */
class S3HostedJdk : VersionedJavaDevelopmentKit {
    private val jdkVersion = "1.8.0"
    private val jdkUpdate = 131
    private val jdkArchive = "jdk${jdkVersion}_$jdkUpdate-linux-x64.tar.gz"
    private val jdkUrl = URI.create("https://s3.amazonaws.com/packages_java/$jdkArchive")
    private val jdkBin = "~/jdk${jdkVersion}_$jdkUpdate/jre/bin/"
    private val bin = "~/jdk${jdkVersion}_$jdkUpdate/bin/"
    override val jstatMonitoring = Jstat(bin)

    override fun getMajorVersion() = 8

    override fun install(connection: SshConnection) {
        download(connection)
        connection.execute("tar -xzf $jdkArchive")
        connection.execute("echo '${use()}' >> ~/.bashrc")
    }

    private fun download(connection: SshConnection) {
        IdempotentAction("download JDK") {
            connection.execute(
                cmd = "curl -s -L -O -k $jdkUrl",
                timeout = Duration.ofMinutes(4)
            )
        }.retry(
            maxAttempts = 3,
            backoff = StaticBackoff(Duration.ofSeconds(4))
        )
    }

    override fun use(): String = "export PATH=$jdkBin:$bin:${'$'}PATH"

    override fun command(options: String) = "${jdkBin}java $options"
}


class StaticBackoff(
    private val backoff: Duration
) : Backoff {

    override fun backOff(attempt: Int): Duration = backoff
}
