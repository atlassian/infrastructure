package com.atlassian.performance.tools.infrastructure.api.storage

import com.atlassian.performance.tools.infrastructure.UbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicJiraSoftwareDistributionsIT {

    @Test
    fun shouldDownloadJiraSoftware() {
        UbuntuContainer().run { ssh ->
            val jiraDistribution: ProductDistribution = PublicJiraSoftwareDistributions().get("7.2.0")
            val targetFolder = "test"
            ssh.execute("mkdir $targetFolder")

            val remoteDirectory = jiraDistribution
                .install(ssh, targetFolder)

            val lsResult = ssh.execute("ls $remoteDirectory").output
            assertThat(lsResult).contains("atlassian-jira")
        }
    }
}