package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.SshConnection
import java.net.URI

interface Database {

    /**
     * @return Database data location if exists
     */
    fun setup(ssh: SshConnection): String

    fun start(jira: URI, ssh: SshConnection)
}
