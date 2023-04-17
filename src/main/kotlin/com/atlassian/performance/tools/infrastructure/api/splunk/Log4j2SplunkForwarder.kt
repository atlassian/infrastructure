package com.atlassian.performance.tools.infrastructure.api.splunk

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.nio.file.Paths

/**
 * Tries to JSONify both log4j v1 and log4j v2. Fails only when both attempts fail.
 */
class Log4j2SplunkForwarder(
    private val log4j2ConfigFileName: String,
    private val splunkForwarder: SplunkForwarder
) : SplunkForwarder by splunkForwarder {

    override fun jsonifyLog4j(sshConnection: SshConnection, log4jPropertiesPath: String) {
        val log4j2PropertiesPath = Paths.get(log4jPropertiesPath)
            .resolveSibling(log4j2ConfigFileName)
            .toString()
        val log4j1Fail = tryToJsonify(sshConnection, log4jPropertiesPath)
        val log4j2Fail = tryToJsonify(sshConnection, log4j2PropertiesPath)
        if (log4j1Fail != null && log4j2Fail != null) {
            log4j1Fail.addSuppressed(log4j2Fail)
            throw log4j1Fail
        }
    }

    private fun tryToJsonify(ssh: SshConnection, log4jPath: String): Throwable? {
        return try {
            splunkForwarder.jsonifyLog4j(ssh, log4jPath)
            null
        } catch (e: Exception) {
            e
        }
    }
}
