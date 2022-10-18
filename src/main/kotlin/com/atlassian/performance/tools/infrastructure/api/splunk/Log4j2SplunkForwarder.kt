package com.atlassian.performance.tools.infrastructure.api.splunk

import com.atlassian.performance.tools.infrastructure.splunk.Log4jJsonifier
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.nio.file.Paths

class Log4j2SplunkForwarder(log4j2ConfigFileName: String, splunkForwarder: SplunkForwarder) :
    SplunkForwarder by splunkForwarder {

    private val log4j2ConfigFileName = log4j2ConfigFileName

    override fun jsonifyLog4j(sshConnection: SshConnection, log4jPropertiesPath: String) {
        val log4j2ConfigPath = Paths.get(log4jPropertiesPath).resolveSibling(log4j2ConfigFileName).toString()
        Log4jJsonifier().jsonifyLog4j1AndLog4j2(sshConnection, log4jPropertiesPath, log4j2ConfigPath)
    }
}
