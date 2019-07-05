package com.atlassian.performance.tools.infrastructure.database

import com.atlassian.performance.tools.ssh.api.SshConnection

internal class SshMysqlClient {

    fun runSql(
        ssh: SshConnection,
        sql: String
    ): SshConnection.SshResult {
        val quotedSql = sql.quote('"')
        return ssh.execute("mysql -h 127.0.0.1 -u root -e $quotedSql")
    }

    private fun String.quote(
        quote: Char
    ): String = quote + escape(quote) + quote

    private fun String.escape(
        character: Char
    ): String = replace(
        oldValue = character.toString(),
        newValue = "\\$character"
    )
}