package com.atlassian.performance.tools.infrastructure.dataset

import com.atlassian.performance.tools.infrastructure.HttpResource
import com.atlassian.performance.tools.infrastructure.Ls
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.dataset.FileArchiver
import com.atlassian.performance.tools.jvmtasks.api.TaskTimer
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.commons.io.FilenameUtils
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.*

@Deprecated("Can be inlined into `com.atlassian.performance.tools.infrastructure.api.dataset.HttpDatasetPackage` " +
    "after `ObsoleteHttpDatasetPackage` removed.")
internal class HttpDatasetPackage(
    private val uri: URI,
    private val timeout: Duration
) : DatasetPackage {

    override fun download(
        ssh: SshConnection
    ): String {
        val resourceName = FilenameUtils.getName(uri.path)
        val downloadDuration = download(ssh, resourceName)
        val timeForUnzipping = timeout.minus(downloadDuration)
        if (timeForUnzipping.isNegative) {
            throw Exception("The archive has been download successfully, but no time to unzip left.")
        }
        return unzip(ssh, resourceName, timeForUnzipping)
    }

    private fun download(ssh: SshConnection, resourceName: String): Duration {
        val start = Instant.now()
        TaskTimer.time("download") {
            HttpResource(uri).download(ssh, resourceName, timeout)
        }
        val afterDownload = Instant.now()
        return Duration.between(start, afterDownload)
    }

    private fun unzip(ssh: SshConnection, resourceName: String, timeForUnzipping: Duration): String {
        val destination = UUID.randomUUID().toString()
        ssh.execute("mkdir $destination")
        FileArchiver().unzip(
            connection = ssh,
            archive = resourceName,
            destination = destination,
            timeout = timeForUnzipping
        )
        val newFiles = Ls().execute(ssh, destination)
        val dataset = (newFiles.singleOrNull()
            ?: throw Exception("Expected one new folder. Found $newFiles."))

        return ssh.execute("cd '$destination/$dataset' && pwd").output.trimEnd('\n')
    }

    override fun toString(): String {
        return "HttpDatasetPackage(uri='$uri', timeout=$timeout)"
    }
}
