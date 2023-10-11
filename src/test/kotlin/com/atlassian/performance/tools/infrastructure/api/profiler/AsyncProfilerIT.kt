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
import java.io.File
import java.nio.file.Files.createTempFile
import java.time.Duration
import java.util.function.Consumer

class AsyncProfilerIT {

    @Test
    fun shouldWorkOnXenial() {
        val profiler = AsyncProfiler.Builder().build()
        testOn(profiler, "16.04") {
            assertFlamegraph(it)
        }
    }

    @Test
    fun shouldWorkOnFocal() {
        val profiler = AsyncProfiler.Builder()
            .output("flat=20,traces=50", "flat.txt")
            .build()
        testOn(profiler, "20.04") {
            assertThat(it.readLines().first().contains("Execution profile"))
        }
    }

    @Test
    fun shouldDumpJfr() {
        val profiler = AsyncProfiler.Builder()
            .jfr("flight.jfr")
            .build()
        testOn(profiler, "20.04") {
            assertThat(it.readLines().first()).contains("FLR")
        }
    }

    @Test
    fun shouldRunInWallClockMode() {
        val profiler = AsyncProfiler.Builder()
            .wallClockMode()
            .interval(Duration.ofMillis(9))
            .flamegraph("flame.html")
            .build()

        testOn(profiler, "20.04") {
            assertFlamegraph(it)
        }
    }

    private fun testOn(profiler: Profiler, ubuntuVersion: String, resultAssert: (File) -> Unit) {
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
                    .download(createTempFile("profiler-result", ".tmp"))
                resultAssert(profilerResult)
            }
        }
    }

    private fun assertFlamegraph(result: File) {
        assertThat(result.readLines().take(5)).contains("<!DOCTYPE html>")
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
