package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.dataset.HttpDatasetPackage
import java.net.URI
import java.time.Duration

class Datasets {

    fun smallJiraSeven() = URI("https://s3-eu-west-1.amazonaws.com/")
        .resolve("jpt-custom-datasets-storage-a008820-datasetbucket-1sjxdtrv5hdhj/")
        .resolve("dataset-f8dba866-9d1b-492e-b76c-f4a78ac3958c/")
        .let { uri ->
            HttpDatasetPackage(
                uri = uri.resolve("database.tar.bz2"),
                downloadTimeout = Duration.ofMinutes(6)
            )
        }
}