package com.atlassian.performance.tools.infrastructure.virtualusers

import com.atlassian.performance.tools.io.ensureParentDirectory
import com.atlassian.performance.tools.ssh.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path

data class DirectResultsTransport(
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
}