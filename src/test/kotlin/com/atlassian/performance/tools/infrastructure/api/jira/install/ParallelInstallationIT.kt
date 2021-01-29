package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jvm.AdoptOpenJDK11
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Files
import java.util.function.Consumer

class ParallelInstallationIT {

    @Test
    fun shouldInstallJira() {
        // given
        val jiraInstallation = ParallelInstallation(
            jiraHomeSource = EmptyJiraHome(),
            productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
            jdk = AdoptOpenJDK11()
        )
        val privatePort = 8080
        val container = SshUbuntuContainer(Consumer {
            it.addExposedPort(privatePort)
        })
        val serverXml = container.start().use useContainer@{ sshUbuntu ->
            val server = TcpServer(
                "localhost",
                sshUbuntu.container.getMappedPort(privatePort),
                privatePort,
                "my-jira"
            )
            return@useContainer sshUbuntu.toSsh().newConnection().use useSsh@{ ssh ->
                // when
                val installed = jiraInstallation.install(ssh, server)

                // then
                val serverXml = Files.createTempFile("downloaded-server", ".xml")
                ssh.download(installed.installation + "/conf/server.xml", serverXml)
                return@useSsh serverXml
            }
        }

        assertThat(serverXml.toFile().readText()).contains("<Connector port=\"8080\"")
    }
}
