package com.atlassian.performance.tools.infrastructure.dataset

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

@Deprecated("The adapter can be removed with a major release.")
internal class ObsoleteHttpDatasetPackage(
    private val downloadPath: String,
    private val unpackedPath: String? = null,
    private val downloadTimeout: Duration
) : DatasetPackage {

    override fun download(
        ssh: SshConnection
    ): String {
        throw UnsupportedOperationException("Download of this HttpDatasetPackage (downloadPath='$downloadPath') could not be executed. Create HttpDatasetPackage instance using the two-parameters constructor rather than the deprecated three-parameters one.")
    }

    override fun toString(): String {
        return "HttpDatasetPackage(downloadPath='$downloadPath', unpackedPath=$unpackedPath, downloadTimeout=$downloadTimeout, datasetBucket=UNSUPPORTED)"
    }
}
