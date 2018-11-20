package com.atlassian.performance.tools.infrastructure.api.dataset

import com.atlassian.performance.tools.jvmtasks.api.TaskTimer.time
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

class HttpDatasetPackage(
    private val downloadPath: String,
    private val unpackedPath: String? = null,
    private val downloadTimeout: Duration
) : DatasetPackage {

    private val datasetBucket = URI("https://s3.eu-central-1.amazonaws.com/jira-soke-tests-eu/")

    override fun download(
        ssh: SshConnection
    ): String {
        val unzipCommand = FileArchiver().pipeUnzip(ssh)
        time("download") {
            ssh.execute("wget -qO - ${datasetBucket.resolve(downloadPath)} | $unzipCommand", downloadTimeout)
        }
        return unpackedPath!!
    }

    override fun toString(): String {
        return "HttpDatasetPackage(downloadPath='$downloadPath', unpackedPath=$unpackedPath, downloadTimeout=$downloadTimeout, datasetBucket=$datasetBucket)"
    }
}