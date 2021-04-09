package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicJiraSoftwareDistributionsIT {

    @Test
    fun shouldDownloadJiraSoftware() {
        DockerInfrastructure().use { infra ->
            infra.serve().newConnection().use { connection ->
                val jiraDistribution: ProductDistribution = PublicJiraSoftwareDistribution("7.2.0")
                val targetFolder = "test"
                connection.execute("mkdir $targetFolder")

                val remoteDirectory = jiraDistribution
                    .install(connection, targetFolder)

                val directories = connection.execute("ls $remoteDirectory").output
                assertThat(directories).contains("atlassian-jira")
            }
        }
    }
}