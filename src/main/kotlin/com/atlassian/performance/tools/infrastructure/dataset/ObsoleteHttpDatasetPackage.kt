package com.atlassian.performance.tools.infrastructure.dataset

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.dataset.FileArchiver
import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.TaskTimer
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

@Deprecated("The adapter can be removed with a major release.")
internal class ObsoleteHttpDatasetPackage(
    private val downloadPath: String,
    private val unpackedPath: String? = null,
    private val downloadTimeout: Duration
) : DatasetPackage {

    private val datasetBucket = URI("https://s3.eu-central-1.amazonaws.com/jira-soke-tests-eu/")

    override fun download(
        ssh: SshConnection
    ): String {
        val unzipCommand = FileArchiver().pipeUnzip(ssh)
        TaskTimer.time("download") {
            IdempotentAction("download dataset") {
                ssh.execute("wget -qO - ${datasetBucket.resolve(downloadPath)} | $unzipCommand", downloadTimeout)
            }.retry(5, ExponentialBackoff(Duration.ofSeconds(2)))
        }
        return unpackedPath!!
    }

    override fun toString(): String {
        return "HttpDatasetPackage(downloadPath='$downloadPath', unpackedPath=$unpackedPath, downloadTimeout=$downloadTimeout, datasetBucket=$datasetBucket)"
    }
}
