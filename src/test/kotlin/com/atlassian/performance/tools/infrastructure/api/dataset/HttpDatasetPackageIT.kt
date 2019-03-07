package com.atlassian.performance.tools.infrastructure.api.dataset

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions
import org.junit.Test
import java.net.URI
import java.time.Duration

class HttpDatasetPackageIT {

    @Test
    fun shouldDownloadDataset() {
        val dataset = HttpDatasetPackage(
                uri = URI("https://s3-eu-west-1.amazonaws.com/jpt-custom-datasets-storage-a008820-datasetbucket-1sjxdtrv5hdhj/af4c7d3b-925c-464c-ab13-79f615158316/database.tar.bz2"),
                downloadTimeout = Duration.ofMinutes(1)
        )

        val unpackedPath = SshUbuntuContainer().start().use { sshUbuntu ->
            return@use sshUbuntu.toSsh().newConnection().use { connection ->
                return@use dataset.download(connection)
            }
        }

        Assertions.assertThat(unpackedPath).isEqualTo("database")
    }
}
