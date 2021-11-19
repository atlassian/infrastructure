package com.atlassian.performance.tools.infrastructure.mock

import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.io.File
import java.util.*

class MockSshSqlClient : SshSqlClient {
    private val log = mutableListOf<String>()
    private val defaultSshResult = SshConnection.SshResult(
        exitStatus = 0,
        output = "",
        errorOutput = ""
    )
    private val returnedCommandResults = ArrayDeque<SshConnection.SshResult>()

    fun queueReturnedSqlCommandResult(result: SshConnection.SshResult) {
        returnedCommandResults.add(result)
    }

    override fun runSql(
        ssh: SshConnection,
        sql: String
    ) = (if (returnedCommandResults.isEmpty()) defaultSshResult else returnedCommandResults.pop())
        .also { log.add(sql) }

    override fun runSql(
        ssh: SshConnection,
        sql: File
    ) = (if (returnedCommandResults.isEmpty()) defaultSshResult else returnedCommandResults.pop())
        .also { log.add(sql.readText()) }

    fun getLog(): List<String> = log
}