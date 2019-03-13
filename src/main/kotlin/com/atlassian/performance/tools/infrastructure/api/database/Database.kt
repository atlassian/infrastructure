package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

interface Database {

    /**
     * @return Database data location if exists
     */
    fun setup(ssh: SshConnection): String

    fun start(jira: URI, ssh: SshConnection)

    fun getDbType() : DbType
}

enum class DbType{
    MySql,
    Postgres
}
