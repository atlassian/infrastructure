package com.atlassian.performance.tools.infrastructure.api.profiler

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNode
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Files.createTempFile

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
            installedJira.host.ssh.newConnection().use { ssh ->
                // when
                profiler.install(ssh)
                val startedJira = JiraLaunchScript().start(installedJira, Reports())
                val process = profiler.start(ssh, startedJira.pid)
                Thread.sleep(5000)
                process.stop(ssh)

                // then
                val profilerResult = RemotePath(installedJira.host.ssh.host, process.getResultPath())
                    .download(createTempFile("profiler-result", ".svg"))
                assertThat(profilerResult.readLines().take(5)).contains("<!DOCTYPE html>")
            }
        }
    }

    private fun <T> testOnInstalledJira(ubuntuVersion: String, test: (InstalledJira) -> T) {
        DockerInfrastructure(ubuntuVersion).use { infra ->
            val jiraNode = infra.serve(777, "jira")
            val installed = ParallelInstallation(
                jiraHomeSource = EmptyJiraHome(),
                productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                jdk = AdoptOpenJDK()
            ).install(jiraNode, Reports())
            test(installed)
        }
    }
}
