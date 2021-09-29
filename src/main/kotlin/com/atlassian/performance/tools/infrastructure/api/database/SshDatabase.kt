package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

/**
 * SSH-based database.
 */
interface SshDatabase {

    /**
     * @return Database data location if exists
     */
    fun setup(ssh: SshConnection): String

    fun start(jira: URI, ssh: SshConnection)
}
