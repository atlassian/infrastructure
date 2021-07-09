package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.Datasets
import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomePackage
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK
import com.atlassian.performance.tools.io.api.resolveSafely
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

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
        val nodePlan = JiraNodePlan.Builder(infrastructure)
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
        val jiraServer = try {
            jiraServerPlan.materialize()
        } catch (e: Exception) {
            debug(jiraServerPlan, e)
        }
        val reports = jiraServerPlan.report().downloadTo(Files.createTempDirectory("jira-server-plan-"))

        // then
        val theNode = jiraServer.nodes.single()
        val serverXml = theNode
            .installed
            .installation
            .resolve("conf/server.xml")
            .download(Files.createTempFile("downloaded-config", ".xml"))
        assertThat(serverXml.readText()).contains("<Connector port=\"${theNode.installed.http.tcp.port}\"")
        assertThat(theNode.pid).isPositive()
        assertThat(reports).isDirectory()
        val fileTree = reports
            .walkTopDown()
            .map { reports.toPath().relativize(it.toPath()) }
            .toList()
        assertThat(fileTree.map { it.toString() }).contains(
            "jira-node/root/atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
            "jira-node/root/~/jpt-jstat.log",
            "jira-node/root/~/jpt-vmstat.log",
            "jira-node/root/~/jpt-iostat.log"
        )
        assertThat(fileTree.filter { it.fileName.toString().startsWith("access_log") })
            .`as`("access logs from $fileTree")
            .isNotEmpty
        assertThat(fileTree.filter { it.fileName.toString().startsWith("atlassian-jira-gc") })
            .`as`("GC logs from $fileTree")
            .isNotEmpty
    }

    private fun debug(
        jiraServerPlan: JiraInstancePlan,
        e: Exception
    ) : Nothing {
        val debugging = Paths.get("build/test-artifacts/")
            .resolveSafely(javaClass.simpleName)
            .resolveSafely(Instant.now().toString())
        try {
            jiraServerPlan.report().downloadTo(debugging)
        } catch (debuggingException: Exception) {
            debuggingException.addSuppressed(e)
            throw debuggingException
        }
        throw Exception("Jira Server plan failed to materialize, debugging info available in $debugging", e)
    }
}
