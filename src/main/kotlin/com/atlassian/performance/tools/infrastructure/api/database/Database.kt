package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

/**
 * SSH-based database.
 */
interface Database {

    @Deprecated(message = "Use setupAndGetLocation instead", replaceWith = ReplaceWith("performSetup(ssh)"))
    fun setup(ssh: SshConnection) = performSetup(ssh).databaseDataLocation

    fun performSetup(ssh: SshConnection): DatabaseSetup

    fun start(jira: URI, ssh: SshConnection)
}
