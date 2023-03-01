package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.HttpDatasetPackage
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test
import java.net.URI
import java.time.Duration
import java.util.function.Consumer

/**
 * Note: We decided to use `Mysql` casing in this repository. Please don't repeat `MySql`.
 */
class MySqlDatabaseIT {
    @Test
    fun shouldStartMysql() {
        val databaseSource = HttpDatasetPackage(
            uri = URI("https://s3-eu-west-1.amazonaws.com/")
                .resolve("jpt-custom-datasets-storage-a008820-datasetbucket-1sjxdtrv5hdhj/")
                .resolve("dataset-f8dba866-9d1b-492e-b76c-f4a78ac3958c/")
                .resolve("database.tar.bz2"),
            downloadTimeout = Duration.ofMinutes(5)
        )
        val mysql = MySqlDatabase(databaseSource)

        SshUbuntuContainer.Builder()
            .customization(Consumer { it.setPrivilegedMode(true) })
            .build()
            .start()
            .use { ubuntu ->
                ubuntu.toSsh().newConnection().use { ssh ->
                    mysql.setup(ssh)
                    mysql.start(URI("https://dummy-jira.net"), ssh)
                }
            }
    }
}
