package com.atlassian.performance.tools.infrastructure.mock

import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.api.database.DatabaseSetup
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class RememberingDatabase : Database {

    var isSetup = false
    var isStarted = false

    override fun setup(ssh: SshConnection): DatabaseSetup {
        isSetup = true
        return DatabaseSetup(location = ".")
    }

    override fun start(jira: URI, ssh: SshConnection) {
        isStarted = true
    }
}