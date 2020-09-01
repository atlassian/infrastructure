package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage.Companion.runSoloSsh
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicJiraSoftwareDistributionsIT {

    @Test
    fun shouldDownloadJiraSoftware() {
        runSoloSsh { ssh ->
            val jiraDistribution: ProductDistribution = PublicJiraSoftwareDistribution("7.2.0")
            val targetFolder = "test"
            ssh.execute("mkdir $targetFolder")

            val remoteDirectory = jiraDistribution.install(ssh, targetFolder)

            val directories = ssh.execute("ls $remoteDirectory").output
            assertThat(directories).contains("atlassian-jira")
        }
    }
}