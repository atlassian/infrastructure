package com.atlassian.performance.tools.infrastructure.mock

import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class RememberingDatabase : Database {

    var isSetup = false
    var isStarted = false

    override fun setup(ssh: SshConnection): String {
        isSetup = true
        return "."
    }

    override fun start(jira: URI, ssh: SshConnection) {
        isStarted = true
    }
}