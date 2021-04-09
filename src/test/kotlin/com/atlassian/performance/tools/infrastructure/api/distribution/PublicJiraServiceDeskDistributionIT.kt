package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicJiraServiceDeskDistributionIT {

    @Test
    fun shouldDownloadJiraServiceDesk() {
        DockerInfrastructure().use { infra ->
            infra.serve().newConnection().use { connection ->
                val serviceDeskDistribution: ProductDistribution = PublicJiraServiceDeskDistribution("4.0.1")
                val targetFolder = "test"
                connection.execute("mkdir $targetFolder")

                val remoteDirectory = serviceDeskDistribution
                    .install(connection, targetFolder)

                val directories = connection.execute("ls $remoteDirectory").output
                assertThat(directories).contains("atlassian-jira")
            }
        }
    }
}