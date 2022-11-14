package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.HttpDatasetPackage
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test
import java.net.URI
import java.time.Duration

class MysqlDatabaseIT {

    @Test
    fun shouldStartMysql() {
        val mysql = MySqlDatabase(
            HttpDatasetPackage(
                uri = URI("https://s3-eu-west-1.amazonaws.com/")
                    .resolve("jpt-custom-datasets-storage-a008820-datasetbucket-1sjxdtrv5hdhj/")
                    .resolve("dataset-f8dba866-9d1b-492e-b76c-f4a78ac3958c/")
                    .resolve("database.tar.bz2"),
                downloadTimeout = Duration.ofMinutes(5)
            )
        )

        SshUbuntuContainer.Builder()
            .enableDocker()
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
