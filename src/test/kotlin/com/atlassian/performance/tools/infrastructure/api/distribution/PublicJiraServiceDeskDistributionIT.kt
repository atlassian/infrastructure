package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicJiraServiceDeskDistributionIT {

    @Test
    fun shouldDownloadJiraServiceDesk() {
        DockerInfrastructure().use { infra ->
            infra.serveSsh().newConnection().use { connection ->
                val distro: ProductDistribution = PublicJiraServiceDeskDistribution("4.0.1")
                val targetFolder = "test"
                connection.execute("mkdir $targetFolder")

                val installation = distro.install(connection, "destination")

                val directories = connection.execute("ls $installation").output
                assertThat(directories).contains("atlassian-jira")
            }
        }
    }
}
