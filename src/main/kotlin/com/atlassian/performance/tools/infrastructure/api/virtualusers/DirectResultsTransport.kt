package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.io.api.ensureParentDirectory
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path

class DirectResultsTransport(
    private val localDestination: Path
) : ResultsTransport {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun transportResults(
        targetDirectory: String,
        sshConnection: SshConnection
    ) {
        logger.info("Downloading results to $localDestination")
        localDestination.toFile().ensureParentDirectory()
        sshConnection.download("$targetDirectory/*", localDestination)
    }

    override fun toString(): String {
        return "DirectResultsTransport(localDestination=$localDestination, logger=$logger)"
    }
}