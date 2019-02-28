package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicJiraSoftwareDistributionsIT {

    @Test
    fun shouldDownloadJiraSoftware() {
        SshUbuntuContainer().start().use { ssh ->
            ssh.toSsh().newConnection().use { connection ->
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