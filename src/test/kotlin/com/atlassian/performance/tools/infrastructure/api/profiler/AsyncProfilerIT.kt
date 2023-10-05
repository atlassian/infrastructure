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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Files.createTempFile
import java.time.Duration
import java.util.function.Consumer

class AsyncProfilerIT {

    @Test
    fun shouldWorkOnXenial() {
        val profiler = AsyncProfiler.Builder().build()
        testOn(profiler, "16.04")
    }

    @Test
    fun shouldWorkOnFocal() {
        val profiler = AsyncProfiler.Builder().build()
        testOn(profiler, "20.04")
    }

    @Test
    fun shouldRunInWallClockMode() {
        val profiler = AsyncProfiler.Builder()
            .wallClockMode()
            .interval(Duration.ofMillis(9))
            .build()

        testOn(profiler, "20.04")
    }

    private fun testOn(profiler: Profiler, ubuntuVersion: String) {
        testOnInstalledJira(ubuntuVersion) { installedJira ->
            val sshClient = installedJira.server.ssh
            sshClient.newConnection().use { ssh ->
                // when
                profiler.install(ssh)
                val startedJira = JiraLaunchScript().start(installedJira)
                val process = profiler.start(ssh, startedJira.pid)
                Thread.sleep(5000)
                process!!.stop(ssh)

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
