package com.atlassian.performance.tools.infrastructure.splunk

import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.ssh.api.SshConnection

class Log4jJsonifier {

    fun jsonifyLog4j1(sshConnection: SshConnection, log4jPropertiesPath: String) {
        Sed().replace(
            connection = sshConnection,
            expression = "NewLineIndentingFilteringPatternLayout",
            output = "layout.JsonLayout",
            file = log4jPropertiesPath
        )
    }
}
