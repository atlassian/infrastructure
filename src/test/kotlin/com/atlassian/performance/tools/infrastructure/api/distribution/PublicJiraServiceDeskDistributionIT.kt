package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicJiraServiceDeskDistributionIT {

    @Test
    fun shouldDownloadJiraServiceDesk() {
        SshUbuntuContainer.Builder().build().start().use { ssh ->
            ssh.toSsh().newConnection().use { connection ->
                val distro = PublicJiraServiceDeskDistribution("4.0.1")

                val installation = distro.install(connection, "destination")

                val directories = connection.execute("ls $installation").output
                assertThat(directories).contains("atlassian-jira")
            }
        }
    }
}
