package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.database.DockerMysqlServer
import com.atlassian.performance.tools.infrastructure.api.dataset.HttpDatasetPackage
import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.instance.JiraDataCenterPlan
import com.atlassian.performance.tools.infrastructure.api.jira.instance.JiraServerPlan
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHooks
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.HookedJiraStart
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URI
import java.nio.file.Files
import java.time.Duration
import java.util.*
import java.util.function.Supplier

class HookedJiraStartIT {

    private lateinit var infrastructure: Infrastructure

    @Before
    fun setUp() {
        infrastructure = DockerInfrastructure()
    }

    @After
    fun tearDown() {
        infrastructure.releaseResources()
    }

    @Test
    fun shouldStartJiraWithHooks() {
        // given
        val hooks = PreInstallHooks.default()
        val installation = HookedJiraInstallation(
            ParallelInstallation(
                jiraHomeSource = EmptyJiraHome(),
                productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                jdk = AdoptOpenJDK()
            ),
            hooks
        )
        val start = HookedJiraStart(JiraLaunchScript(), hooks.preStart)

        // when
        val jiraServer = JiraServerPlan.Builder(infrastructure)
            .installation(installation)
            .start(start)
            .hooks(hooks)
            .build()
            .get()

        val host = jiraServer.node.installed.host
        val reports = host.ssh.newConnection().use { ssh ->
            hooks.reports.list().flatMap { it.locate(ssh) }
        }

        // then
        val serverXml = jiraServer
            .node
            .installed
            .installation
            .resolve("conf/server.xml")
            .download(Files.createTempFile("downloaded-config", ".xml"))
        assertThat(serverXml.readText()).contains("<Connector port=\"${host.privatePort}\"")
        assertThat(jiraServer.node.pid).isPositive()
        assertThat(reports).contains(
            "jira-home/log/atlassian-jira.log",
            "./atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
            "~/jpt-jstat.log",
            "~/jpt-vmstat.log",
            "~/jpt-iostat.log"
        )
    }

    @Test
    fun shouldStartDataCenter() {
        // given
        val nodePlans = listOf(1, 2).map {
            val hooks = PreInstallHooks.default()
            JiraNodePlan.Builder()
                .installation(HookedJiraInstallation(
                    ParallelInstallation(
                        jiraHomeSource = EmptyJiraHome(),
                        productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                        jdk = AdoptOpenJDK()
                    ),
                    hooks
                ))
                .start(HookedJiraStart(JiraLaunchScript(), hooks.preStart))
                .hooks(hooks) // TODO but what if I don't pass it?
                .build()
        }
        val instanceHooks = PreInstanceHooks(nodePlans.map { it.hooks })
        val smallJiraSeven = URI("https://s3-eu-west-1.amazonaws.com/")
            .resolve("jpt-custom-datasets-storage-a008820-datasetbucket-1sjxdtrv5hdhj/")
            .resolve("dataset-f8dba866-9d1b-492e-b76c-f4a78ac3958c/")
            .let { uri ->
                HttpDatasetPackage(
                    uri = uri.resolve("database.tar.bz2"),
                    downloadTimeout = Duration.ofMinutes(6)
                )
            }
        instanceHooks.insert(DockerMysqlServer.Builder(infrastructure, smallJiraSeven).build())
        val dcPlan = JiraDataCenterPlan(nodePlans, instanceHooks, Supplier { TODO() }, infrastructure)

        // when
        val dataCenter = dcPlan.get()
        val reports = dataCenter.nodes.ssh.newConnection().use { ssh ->
            nodeHooks.reports.list().flatMap { it.locate(ssh) }
        }

        // then
        dataCenter.nodes.forEach { node ->
            val installed = node.installed
            val serverXml = installed
                .installation
                .resolve("conf/server.xml")
                .download(Files.createTempFile("downloaded-config", ".xml"))
            assertThat(serverXml.readText()).contains("<Connector port=\"${installed.host.privatePort}\"")
            assertThat(node.pid).isPositive()
            assertThat(reports).contains(
                "jira-home/log/atlassian-jira.log",
                "./atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
                "~/jpt-jstat.log",
                "~/jpt-vmstat.log",
                "~/jpt-iostat.log"
            )
        }
    }

}
