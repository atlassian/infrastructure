package com.atlassian.performance.tools.infrastructure.api.splunk

import com.atlassian.performance.tools.ssh.api.SshConnection

class DisabledSplunkForwarder : SplunkForwarder {

    override fun run(sshConnection: SshConnection, name: String, logsPath: String) {
        return
    }

    override fun jsonifyLog4j(sshConnection: SshConnection, log4jPropertiesPath: String) {
        return
    }

    override fun jsonifyLog4j(sshConnection: SshConnection, log4jPropertiesPath: String, log4j2ConfigPath: String) {
        return
    }

    override fun getRequiredPorts(): List<Int> {
        return emptyList()
    }
}
