package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.database.DockerMysqlServer
import com.atlassian.performance.tools.infrastructure.api.dataset.HttpDatasetPackage
import com.atlassian.performance.tools.infrastructure.api.jira.JiraLaunchTimeouts
import com.atlassian.performance.tools.infrastructure.api.jira.instance.PreInstanceHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.RestUpgrade
import com.atlassian.performance.tools.infrastructure.api.network.TcpServerRoom
import java.net.URI
import java.time.Duration

class Datasets {

    object JiraSevenDataset {
        private val s3Bucket = URI("https://s3-eu-west-1.amazonaws.com/")
            .resolve("jpt-custom-datasets-storage-a008820-datasetbucket-1sjxdtrv5hdhj/")
            .resolve("dataset-f8dba866-9d1b-492e-b76c-f4a78ac3958c/")

        private val mysql = HttpDatasetPackage(
            uri = s3Bucket.resolve("database.tar.bz2"),
            downloadTimeout = Duration.ofMinutes(6)
        )

        val jiraHome = HttpDatasetPackage(
            uri = s3Bucket.resolve("jirahome.tar.bz2"),
            downloadTimeout = Duration.ofMinutes(6)
        )

        fun hookMysql(preInstanceHooks: PreInstanceHooks, serverRoom: TcpServerRoom) {
            val mysqlServer = DockerMysqlServer.Builder(serverRoom, mysql)
                .mysqlVersion("5.6.42")
                .build()
            preInstanceHooks.insert(mysqlServer)
        }

        fun hookMysql(postStartHooks: PostStartHooks) {
            val timeouts = JiraLaunchTimeouts.Builder()
                .initTimeout(Duration.ofMinutes(4))
                .build()
            val dataUpgrade = RestUpgrade(timeouts, "admin", "admin")
            postStartHooks.insert(dataUpgrade)
        }
    }

    object SmallJiraEightDataset {
        private val s3Bucket = URI("https://s3-eu-central-1.amazonaws.com/")
            .resolve("jpt-custom-datasets-storage-a008820-datasetbucket-1nrja8d1upind/")
            .resolve("dataset-a533e558-e5c5-46e7-9398-5aeda84d793a/")

        private val mysql = HttpDatasetPackage(
            uri = s3Bucket.resolve("database.tar.bz2"),
            downloadTimeout = Duration.ofMinutes(6)
        )

        val jiraHome = HttpDatasetPackage(
            uri = s3Bucket.resolve("jirahome.tar.bz2"),
            downloadTimeout = Duration.ofMinutes(6)
        )

        fun hookMysql(preInstanceHooks: PreInstanceHooks, serverRoom: TcpServerRoom) {
            val mysqlServer = DockerMysqlServer.Builder(serverRoom, mysql).build()
            preInstanceHooks.insert(mysqlServer)
        }

        fun hookDataUpgrade(postStartHooks: PostStartHooks) {
            val timeouts = JiraLaunchTimeouts.Builder()
                .initTimeout(Duration.ofMinutes(4))
                .build()
            val dataUpgrade = RestUpgrade(timeouts, "admin", "admin")
            postStartHooks.insert(dataUpgrade)
        }
    }
}