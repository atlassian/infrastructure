package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.jira.JiraGcLog
import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.SetenvSh
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicJiraSoftwareDistributionsIT {

    @Test
    fun shouldDownloadJiraSoftware() {
        DockerInfrastructure().use { infra ->
            infra.serveSsh().newConnection().use { connection ->
                val distro: ProductDistribution = PublicJiraSoftwareDistribution("7.2.0")
                val targetFolder = "test"
                connection.execute("mkdir $targetFolder")

                val installation = distro.install(connection, "destination")

                val directories = connection.execute("ls $installation").output
                assertThat(directories).contains("atlassian-jira")
            }
        }
    }

    @Test
    fun shouldInstallReleaseCandidate() {
        DockerInfrastructure().use { infra ->
            infra.serveSsh().newConnection().use { connection ->
                val distro = PublicJiraSoftwareDistribution("9.0.0-RC01")

                val installation = distro.install(connection, "destination")

                val dummyConfig = JiraNodeConfig.Builder().build()
                val dummyGc = JiraGcLog(installation)
                val dummyIp = "1.2.3.4"
                SetenvSh(installation).setup(connection, dummyConfig, dummyGc, dummyIp)
            }
        }
    }
}
