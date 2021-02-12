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
        val installation = ParallelInstallation(
            jiraHomeSource = EmptyJiraHome(),
            productDistribution = PublicJiraSoftwareDistribution("7.13.0"),
            jdk = AdoptOpenJDK11()
        )

        testOnServer { server ->
            // when
            val installed = installation.install(server)

            // then
            val serverXml = installed
                .installation
                .resolve("conf/server.xml")
                .download(Files.createTempFile("downloaded-config", ".xml"))
            assertThat(serverXml.readText()).contains("<Connector port=\"${server.privatePort}\"")
        }
    }

    private fun <T> testOnServer(test: (TcpServer) -> T) {
        val privatePort = 8080
        val container = SshUbuntuContainer(Consumer {
            it.addExposedPort(privatePort)
        })
        container.start().use { sshUbuntu ->
            val server = TcpServer(
                "localhost",
                sshUbuntu.container.getMappedPort(privatePort),
                privatePort,
                "my-jira",
                sshUbuntu.toSsh()
            )
            test(server)
        }
    }
}
