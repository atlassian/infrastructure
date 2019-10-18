package com.atlassian.performance.tools.infrastructure.api.storage

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import org.assertj.core.api.Assertions
import org.junit.Test

class PublicJiraSoftwareDistributionsTest {

    @Test
    fun shouldDelegateToPublicJiraSoftwareDistribution() {
        val jiraSoftwareVersion = "7.2.0"
        @Suppress("DEPRECATION") val jiraDistributionFromOldApi: ProductDistribution = PublicJiraSoftwareDistributions().get(jiraSoftwareVersion)
        val jiraDistribution = PublicJiraSoftwareDistribution(jiraSoftwareVersion)

        val sshOldApi = RememberingSshConnection()
        jiraDistributionFromOldApi.install(sshOldApi, "destination")

        val sshNewApi = RememberingSshConnection()
        jiraDistribution.install(sshNewApi, "destination")

        Assertions.assertThat(sshOldApi.commands).containsAll(sshNewApi.commands)
    }
}
