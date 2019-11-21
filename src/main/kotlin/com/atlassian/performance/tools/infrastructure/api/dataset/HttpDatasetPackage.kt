package com.atlassian.performance.tools.infrastructure.api.dataset

import com.atlassian.performance.tools.infrastructure.dataset.HttpDatasetPackage
import com.atlassian.performance.tools.infrastructure.dataset.ObsoleteHttpDatasetPackage
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

/**
 * Downloads and unzips dataset on the remote machine.
 */
class HttpDatasetPackage private constructor(
    private val datasetPackage: DatasetPackage
) : DatasetPackage {

    @Suppress("DEPRECATION")
    @Deprecated("Use two-parameters constructor.")
    constructor(
        downloadPath: String,
        unpackedPath: String? = null,
        downloadTimeout: Duration
    ) : this(
        ObsoleteHttpDatasetPackage(
            downloadPath = downloadPath,
            unpackedPath = unpackedPath,
            downloadTimeout = downloadTimeout
        )
    )

    constructor(
        uri: URI,
        downloadTimeout: Duration
    ) : this(
        HttpDatasetPackage(
            uri = uri,
            timeout = downloadTimeout
        )
    )

    override fun download(
        ssh: SshConnection
    ): String {
        return datasetPackage.download(ssh)
    }

    override fun toString(): String {
        return datasetPackage.toString()
    }
}
