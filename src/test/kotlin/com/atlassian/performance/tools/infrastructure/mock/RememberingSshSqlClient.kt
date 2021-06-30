package com.atlassian.performance.tools.infrastructure.mock

import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.io.File

class RememberingSshSqlClient : SshSqlClient {
    private val log = mutableListOf<String>()

    override fun runSql(
        ssh: SshConnection,
        sql: String
    ) = SshConnection.SshResult(
        exitStatus = 0,
        output = "",
        errorOutput = ""
    ).also { log.add(sql) }

    override fun runSql(
        ssh: SshConnection,
        sql: File
    ) = SshConnection.SshResult(
        exitStatus = 0,
        output = "",
        errorOutput = ""
    ).also { log.add(sql.readText()) }

    fun getLog(): List<String> = log
}