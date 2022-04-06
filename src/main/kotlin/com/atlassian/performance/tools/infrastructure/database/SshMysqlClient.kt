package com.atlassian.performance.tools.infrastructure.database

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.io.File

internal class SshMysqlClient(
    private val host: String,
    private val port: Int,
    private val user: String
) : SshSqlClient {

    internal constructor() : this("127.0.0.1", 3306, "root")

    override fun runSql(
        ssh: SshConnection,
        sql: String
    ): SshConnection.SshResult {
        val quotedSql = sql.quote('"')
        return ssh.execute("mysql -h $host -P $port -u $user -e $quotedSql")
    }

    override fun runSql(
        ssh: SshConnection,
        sql: File
    ): SshConnection.SshResult {
        val remoteSqlFile = sql.name
        ssh.upload(sql, remoteSqlFile)
        val result = ssh.execute("mysql -h $host -P $port -u $user < $remoteSqlFile")
        ssh.execute("rm $remoteSqlFile")
        return result
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