package com.atlassian.performance.tools.infrastructure.api.profiler

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath
import com.atlassian.performance.tools.infrastructure.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.jira.install.TcpServer
import com.atlassian.performance.tools.infrastructure.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Volume
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Files.createTempFile
import java.util.function.Consumer

class AsyncProfilerIT {

    @Test
    fun shouldWorkOnXenial() {
        testOn("16.04")
    }

    @Test
    fun shouldWorkOnFocal() {
        testOn("20.04")
    }

    private fun testOn(ubuntuVersion: String) {
        // given
        val profiler = AsyncProfiler()

        testOnInstalledJira(ubuntuVersion) { installedJira ->
            val sshClient = installedJira.server.ssh
            sshClient.newConnection().use { ssh ->
                // when
                profiler.install(ssh)
                val startedJira = JiraLaunchScript().start(installedJira)
                val process = profiler.start(ssh, startedJira.pid)
                Thread.sleep(5000)
                process.stop(ssh)

                // then
                val profilerResult = RemotePath(sshClient.host, process.getResultPath())
                    .download(createTempFile("profiler-result", ".svg"))
                assertThat(profilerResult.readLines().take(5)).contains("<!DOCTYPE html>")
            }
        }
    }

    private fun <T> testOnInstalledJira(ubuntuVersion: String, test: (InstalledJira) -> T) {
        val privatePort = 8080
        val container = SshUbuntuContainer.Builder()
            .version(ubuntuVersion)
            .customization(Consumer {
                it.addExposedPort(privatePort)
                it.setPrivilegedMode(true)
                val dockerDaemonSocket = "/var/run/docker.sock"
                it.setBinds(listOf(Bind(dockerDaemonSocket, Volume(dockerDaemonSocket))))
            })
            .build()
        container.start().use { sshUbuntu ->
            val server = TcpServer(
                "localhost",
                sshUbuntu.container.getMappedPort(privatePort),
                privatePort,
                "my-jira",
                sshUbuntu.toSsh()
            )
            val installed = ParallelInstallation(
                jiraHomeSource = EmptyJiraHome(),
                productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                jdk = AdoptOpenJDK()
            ).install(server)
            test(installed)
        }
    }
}
