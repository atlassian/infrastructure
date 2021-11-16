package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

/**
 * SSH-based database.
 */
interface Database {

    fun setup(ssh: SshConnection): DatabaseSetup

    fun start(jira: URI, ssh: SshConnection)
}
