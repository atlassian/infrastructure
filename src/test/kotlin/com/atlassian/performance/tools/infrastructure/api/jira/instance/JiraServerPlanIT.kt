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

class JiraServerPlanIT {

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
    fun shouldStartJiraWithHooks() {
        // given
        val hooks = PreInstallHooks.default()
            .also { Datasets.JiraSevenDataset.hookMysql(it.postStart) }
        val nodePlan = JiraNodePlan.Builder()
            .hooks(hooks)
            .installation(
                ParallelInstallation(
                    jiraHomeSource = JiraHomePackage(Datasets.JiraSevenDataset.jiraHome),
                    productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                    jdk = AdoptOpenJDK()
                )
            )
            .start(JiraLaunchScript())
            .hooks(hooks)
            .build()
        val instanceHooks = PreInstanceHooks.default()
            .also { Datasets.JiraSevenDataset.hookMysql(it, infrastructure) }
        val jiraServerPlan = JiraServerPlan.Builder(infrastructure)
            .plan(nodePlan)
            .hooks(instanceHooks)
            .build()

        // when
        val jiraServer = jiraServerPlan.materialize()

        val theNode = jiraServer.nodes.single()
        val host = theNode.installed.host
        val downloadedReports = jiraServerPlan.report().downloadTo(Files.createTempDirectory("jira-server-plan-"))

        // then
        val serverXml = theNode
            .installed
            .installation
            .resolve("conf/server.xml")
            .download(Files.createTempFile("downloaded-config", ".xml"))
        assertThat(serverXml.readText()).contains("<Connector port=\"${host.port}\"")
        assertThat(theNode.pid).isPositive()
        assertThat(downloadedReports.resolve("jira-node-1").list()).contains(
            "jira-home/log/atlassian-jira.log",
            "./atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
            "~/jpt-jstat.log",
            "~/jpt-vmstat.log",
            "~/jpt-iostat.log"
        )
    }
}
