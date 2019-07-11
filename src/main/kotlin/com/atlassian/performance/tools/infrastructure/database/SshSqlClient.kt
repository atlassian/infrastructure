package com.atlassian.performance.tools.infrastructure.database

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.io.File

interface SshSqlClient {

    fun runSql(
        ssh: SshConnection,
        sql: String
    ): SshConnection.SshResult

    fun runSql(
        ssh: SshConnection,
        sql: File
    ): SshConnection.SshResult
}
