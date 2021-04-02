package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.Datasets
import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomePackage
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.util.function.Supplier

class JiraDataCenterPlanIT {

    private lateinit var infrastructure: Infrastructure

    @Before
    fun setUp() {
        infrastructure = DockerInfrastructure()
    }

    @After
    fun tearDown() {
        infrastructure.close()
    }

    @Test
    fun shouldStartDataCenter() {
        // given
        val nodePlans = listOf(1, 2).map {
            JiraNodePlan.Builder()
                .installation(
                    ParallelInstallation(
                        jiraHomeSource = JiraHomePackage(Datasets.JiraSevenDataset.jiraHome),
                        productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                        jdk = AdoptOpenJDK()
                    )
                )
                .start(JiraLaunchScript())
                .hooks(PreInstallHooks.default().also { Datasets.JiraSevenDataset.hookMysql(it.postStart) })
                .build()
        }
        val instanceHooks = PreInstanceHooks.default()
            // TODO this plus `EmptyJiraHome()` = failing `DatabaseIpConfig` - couple them together or stop expecting a preexisting `dbconfig.xml`? but then what about missing lucene indexes?
            .also { Datasets.JiraSevenDataset.hookMysql(it, infrastructure) }
        val dcPlan = JiraDataCenterPlan(nodePlans, instanceHooks, Supplier { TODO() }, infrastructure)

        try {
            // when
            val dataCenter = dcPlan.materialize()

            // then
            dataCenter.nodes.forEach { node ->
                val installed = node.installed
                val serverXml = installed
                    .installation
                    .resolve("conf/server.xml")
                    .download(Files.createTempFile("downloaded-config", ".xml"))
                assertThat(serverXml.readText()).contains("<Connector port=\"${installed.host.port}\"")
                assertThat(node.pid).isPositive()
            }
        } finally {
            val reports = dcPlan.report().downloadTo(Files.createTempDirectory("jira-dc-plan-"))
            assertThat(reports).isDirectory()
            assertThat(reports.resolve("jira-node-1").list()).contains(
                "jira-home/log/atlassian-jira.log",
                "./atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
                "~/jpt-jstat.log",
                "~/jpt-vmstat.log",
                "~/jpt-iostat.log"
            )
        }
    }
}
