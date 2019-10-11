package com.atlassian.performance.tools.infrastructure.api.storage

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class PublicJiraSoftwareDistributionsIT {

    @Test
    @Ignore
    fun shouldDownloadJiraSoftware() {
        SshUbuntuContainer().start().use { ssh ->
            ssh.toSsh().newConnection().use { connection ->
                @Suppress("DEPRECATION") val jiraDistribution: ProductDistribution = PublicJiraSoftwareDistributions().get("7.2.0")
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
