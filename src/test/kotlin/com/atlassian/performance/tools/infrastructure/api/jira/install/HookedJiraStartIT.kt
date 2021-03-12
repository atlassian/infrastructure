package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.HookedJiraStart
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Files
import java.util.function.Consumer

class HookedJiraStartIT {

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

        testOnServer("jira", 8080) { server ->
            // when
            val installed = installation.install(server)
            val started = start.start(installed)
            val reports = server.ssh.newConnection().use { ssh ->
                hooks.reports.list().flatMap { it.locate(ssh) }
            }

            // then
            val serverXml = installed
                .installation
                .resolve("conf/server.xml")
                .download(Files.createTempFile("downloaded-config", ".xml"))
            assertThat(serverXml.readText()).contains("<Connector port=\"${server.privatePort}\"")
            assertThat(started.pid).isPositive()
            assertThat(reports).contains(
                "jira-home/log/atlassian-jira.log",
                "./atlassian-jira-software-7.13.0-standalone/logs/catalina.out",
                "~/jpt-jstat.log",
                "~/jpt-vmstat.log",
                "~/jpt-iostat.log"
            )
        }
    }

    @Test
    fun shouldStartDataCenter() {
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

        testOnServer("jira1", 8080) { jira1 ->
            testOnServer("jira2", 8080) { jira2 ->
                testOnServer("mysql", 3306) { mysql ->
                    // when
                    val installed = installation.install(server)
                    val started = start.start(installed)
                    val reports = server.ssh.newConnection().use { ssh ->
                        hooks.reports.list().flatMap { it.locate(ssh) }
                    }

                    // then
                    val serverXml = installed
                        .installation
                        .resolve("conf/server.xml")
                        .download(Files.createTempFile("downloaded-config", ".xml"))
                    assertThat(serverXml.readText()).contains("<Connector port=\"${server.privatePort}\"")
                    assertThat(started.pid).isPositive()
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
    }

    private fun <T> testOnServer(name: String, privatePort: Int, test: (TcpServer) -> T) {
        val container = SshUbuntuContainer(Consumer {
            it.addExposedPort(privatePort)
        })
        container.start().use { sshUbuntu ->
            val server = TcpServer(
                "localhost",
                sshUbuntu.container.getMappedPort(privatePort),
                privatePort,
                name,
                sshUbuntu.toSsh()
            )
            test(server)
        }
    }
}
