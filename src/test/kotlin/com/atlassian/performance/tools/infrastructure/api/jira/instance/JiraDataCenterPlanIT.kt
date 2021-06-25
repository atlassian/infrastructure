package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.Datasets
import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.Infrastructure
import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomePackage
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.sharedhome.SambaSharedHome
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK
import com.atlassian.performance.tools.infrastructure.api.loadbalancer.ApacheProxyPlan
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.time.Duration.ofMinutes

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
        val jiraHomeSource = JiraHomePackage(Datasets.JiraSevenDataset.jiraHome)
        val nodePlans = listOf(1, 2).map {
            val nodeHooks = PreInstallHooks.default()
                .also { Datasets.JiraSevenDataset.hookMysql(it.postStart) }
            JiraNodePlan.Builder()
                .installation(
                    ParallelInstallation(
                        jiraHomeSource = jiraHomeSource,
                        productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
                        jdk = AdoptOpenJDK()
                    )
                )
                .start(JiraLaunchScript())
                .hooks(nodeHooks)
                .build()
        }
        val instanceHooks = PreInstanceHooks.default()
            .also { Datasets.JiraSevenDataset.hookMysql(it, infrastructure) }
            .also { it.insert(SambaSharedHome(jiraHomeSource, infrastructure)) }
        val dcPlan = JiraDataCenterPlan.Builder(infrastructure)
            .nodePlans(nodePlans)
            .instanceHooks(instanceHooks)
            .balancerPlan(ApacheProxyPlan(infrastructure))
            .build()

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
            installed.host.ssh.newConnection().use { ssh ->
                ssh.execute("wget ${dataCenter.address}")
            }
        }
    }


    @Test
    fun shouldProvideLogsToDiagnoseFailure() {
        // given
        class FailingHook : PostStartHook {
            override fun call(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks, reports: Reports) {
                val installed = jira.installed
                ssh.execute("${installed.jdk.use()}; ${installed.installation.path}/bin/stop-jira.sh", ofMinutes(1))
                throw Exception("Failing deliberately after Jira started")
            }
        }

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
                .hooks(PreInstallHooks.default().also { it.postStart.insert(FailingHook()) })
                .build()
        }
        val dcPlan = JiraDataCenterPlan.Builder(infrastructure)
            .nodePlans(nodePlans)
            .build()

        // when
        val thrown = catchThrowable {
            dcPlan.materialize()
        }

        val reports = dcPlan.report().downloadTo(Files.createTempDirectory("jira-dc-plan-"))
        // then
        assertThat(thrown).hasMessageStartingWith("Failing deliberately")
        assertThat(reports).isDirectory()
        val fileTree = reports
            .walkTopDown()
            .map { reports.toPath().relativize(it.toPath()) }
            .toList()
        assertThat(fileTree.map { it.toString() }).contains(
            "jira-node-1/root/atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
            "jira-node-1/root/~/jpt-jstat.log",
            "jira-node-2/root/atlassian-jira-software-7.13.0-standalone/logs/catalina.out"
        )
        assertThat(fileTree.filter { it.fileName.toString() == "atlassian-jira.log" })
            .`as`("Jira log from $fileTree")
            .isNotEmpty
        assertThat(fileTree.filter { it.fileName.toString().startsWith("atlassian-jira-gc") })
            .`as`("GC logs from $fileTree")
            .isNotEmpty
    }
}
