package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.Datasets
import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomePackage
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.sharedhome.SambaSharedHome
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK
import com.atlassian.performance.tools.infrastructure.api.loadbalancer.ApacheProxyPlan
import com.atlassian.performance.tools.io.api.resolveSafely
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant

class JiraPlanIT {

    private lateinit var infrastructure: DockerInfrastructure
    private val dataset = Datasets.SmallJiraEightDataset
    private val jiraVersion = "9.4.9"
    private val jiraDistribution = PublicJiraSoftwareDistribution(jiraVersion)

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
        val jiraHomeSource = JiraHomePackage(dataset.jiraHome)
        val nodePlans = listOf(1, 2).map {
            val nodeHooks = PreInstallHooks.default()
                .also { dataset.hookMysql(it.postStart) }
            JiraNodePlan.Builder(infrastructure)
                .installation(
                    ParallelInstallation(
                        jiraHomeSource = jiraHomeSource,
                        productDistribution = jiraDistribution,
                        jdk = AdoptOpenJDK()
                    )
                )
                .start(JiraLaunchScript())
                .hooks(nodeHooks)
                .build()
        }
        val instanceHooks = PreInstanceHooks.default()
            .also { dataset.hookMysql(it, infrastructure) }
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
            assertThat(serverXml.readText()).contains("<Connector port=\"${installed.http.tcp.port}\"")
            assertThat(node.pid).isPositive()
            val fakeVu = infrastructure.serveSsh()
            fakeVu.newConnection().use { ssh ->
                ssh.execute("wget ${dataCenter.address.addressPrivately()}")
            }
        }
        dataCenter.address.addressPublicly().toURL().readText()
    }


    @Test
    fun shouldProvideDataCenterLogsToDiagnoseFailure() {
        // given
        class FailingHook : PostStartHook {
            override fun call(ssh: SshConnection, jira: StartedJira, hooks: PostStartHooks, reports: Reports) {
                val installed = jira.installed
                ssh.execute(
                    "${installed.jdk.use()}; ${installed.installation.path}/bin/stop-jira.sh",
                    Duration.ofMinutes(1)
                )
                throw Exception("Failing deliberately after Jira started")
            }
        }

        val nodePlans = listOf(1, 2).map {
            JiraNodePlan.Builder(infrastructure)
                .installation(
                    ParallelInstallation(
                        jiraHomeSource = JiraHomePackage(dataset.jiraHome),
                        productDistribution = jiraDistribution,
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
        val fileTree = reports
            .walkTopDown()
            .map { reports.toPath().relativize(it.toPath()) }
            .toList()
        assertSoftly {
            it.assertThat(thrown).hasMessageStartingWith("Failing deliberately")
            it.assertThat(reports).isDirectory()
            it.assertThat(fileTree.map { it.toString() }).contains(
                "jira-node-1/atlassian-jira-software-$jiraVersion-standalone/logs/catalina.out",
                "jira-node-1/~/jpt-jstat.log",
                "jira-node-2/atlassian-jira-software-$jiraVersion-standalone/logs/catalina.out"
            )
            it.assertThat(fileTree.filter { it.fileName.toString() == "atlassian-jira.log" })
                .`as`("Jira log from $fileTree")
                .isNotEmpty
            it.assertThat(fileTree.filter { it.fileName.toString().startsWith("atlassian-jira-gc") })
                .`as`("GC logs from $fileTree")
                .isNotEmpty
        }
    }


    @Test
    fun shouldStartJiraServerWithHooks() {
        // given
        val hooks = PreInstallHooks.default()
            .also { dataset.hookDataUpgrade(it.postStart) }
        val nodePlan = JiraNodePlan.Builder(infrastructure)
            .hooks(hooks)
            .installation(
                ParallelInstallation(
                    jiraHomeSource = JiraHomePackage(dataset.jiraHome),
                    productDistribution = jiraDistribution,
                    jdk = AdoptOpenJDK()
                )
            )
            .start(JiraLaunchScript())
            .hooks(hooks)
            .build()
        val instanceHooks = PreInstanceHooks.default()
            .also { dataset.hookMysql(it, infrastructure) }
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

        assertSoftly {
            it.assertThat(serverXml.readText()).contains("<Connector port=\"${theNode.installed.http.tcp.port}\"")
            it.assertThat(theNode.pid).isPositive()
            it.assertThat(reports).isDirectory()
            val fileTree = reports
                .walkTopDown()
                .map { reports.toPath().relativize(it.toPath()) }
                .toList()
            it.assertThat(fileTree.map { it.toString() }).contains(
                "jira-node/atlassian-jira-software-$jiraVersion-standalone/logs/catalina.out",
                "jira-node/~/jpt-jstat.log",
                "jira-node/~/jpt-vmstat.log",
                "jira-node/~/jpt-iostat.log"
            )
            it.assertThat(fileTree.filter { it.fileName.toString().startsWith("access_log") })
                .`as`("access logs from $fileTree")
                .isNotEmpty
            it.assertThat(fileTree.filter { it.fileName.toString().startsWith("atlassian-jira-gc") })
                .`as`("GC logs from $fileTree")
                .isNotEmpty
        }
    }

    private fun debug(
        jiraServerPlan: JiraInstancePlan,
        e: Exception
    ): Nothing {
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
