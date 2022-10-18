package com.atlassian.performance.tools.infrastructure.splunk

import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class Log4jJsonifier {

    private val logger: Logger = LogManager.getLogger(Log4jJsonifier::class.java)

    fun jsonifyLog4j1(sshConnection: SshConnection, log4jPropertiesPath: String) {
        Sed().replace(
            connection = sshConnection,
            expression = "NewLineIndentingFilteringPatternLayout",
            output = "layout.JsonLayout",
            file = log4jPropertiesPath
        )
    }

    fun jsonifyLog4j1AndLog4j2(sshConnection: SshConnection, log4jPropertiesPath: String, log4j2ConfigPath: String) {
        val log4j1Result = Sed().safeReplace(
            connection = sshConnection,
            expression = "NewLineIndentingFilteringPatternLayout",
            output = "layout.JsonLayout",
            file = log4jPropertiesPath
        )
        if (!log4j1Result.isSuccessful()) {
            logger.debug("Attempt to jsonify $log4jPropertiesPath was unsuccessful.")
        }
        val log4j2Result = Sed().safeReplaceXmlTag(
            connection = sshConnection,
            sourceTagName = "PatternLayout",
            replacementString = "<AtlassianJsonLayout  filteringApplied=\"false\"/>",
            file = log4j2ConfigPath
        )
        if (!log4j1Result.isSuccessful()) {
            logger.debug("Attempt to jsonify $log4j2ConfigPath was unsuccessful.")
        }
        if (!log4j1Result.isSuccessful() && !log4j2Result.isSuccessful()) {
            throw Exception("Failed to jsonify any log4j configuration.")
        }
        if (log4j1Result.isSuccessful() && log4j2Result.isSuccessful()) {
            logger.debug("Migrated both log4j1 and log4j2 config files, ensure that's correct.")
        }
    }
}
