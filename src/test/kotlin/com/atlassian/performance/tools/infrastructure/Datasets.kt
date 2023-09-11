package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.hookapi.database.DockerMysqlServer
import com.atlassian.performance.tools.infrastructure.api.dataset.HttpDatasetPackage
import com.atlassian.performance.tools.infrastructure.api.jira.JiraLaunchTimeouts
import com.atlassian.performance.tools.infrastructure.hookapi.jira.instance.PreInstanceHooks
import com.atlassian.performance.tools.infrastructure.hookapi.jira.start.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.hookapi.jira.start.hook.RestUpgrade
import com.atlassian.performance.tools.infrastructure.api.network.TcpServerRoom
import java.net.URI
import java.time.Duration

class Datasets {
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
            // encrypted with atlassian-password-encoder
            val encryptedAdmin = "{PKCS5S2}dHH7Ws1DcJ1H4d9C8BN1Kh83ciEXVy025l9mIM8P3mlseybpKtI83531tOIyE/gb"
            val mysqlServer = DockerMysqlServer.Builder(serverRoom, mysql)
                .setPassword("admin", encryptedAdmin)
                .resetCaptcha("admin")
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

        fun hookDataUpgrade(postStartHooks: PostStartHooks) {
            val timeouts = JiraLaunchTimeouts.Builder()
                .initTimeout(Duration.ofMinutes(4))
                .build()
            val dataUpgrade = RestUpgrade(timeouts, "admin", "admin")
            postStartHooks.insert(dataUpgrade)
        }
    }
}
