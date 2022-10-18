package com.atlassian.performance.tools.infrastructure.api.splunk

import com.atlassian.performance.tools.ssh.api.SshConnection

interface SplunkForwarder {
    fun run(sshConnection: SshConnection, name: String, logsPath: String)
    fun jsonifyLog4j(sshConnection: SshConnection, log4jPropertiesPath: String)
    fun getRequiredPorts(): List<Int>
}
