package com.atlassian.performance.tools.infrastructure.api.database.captcha

import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI


/**
 * [Reset a user's login count from the database in Jira server](https://confluence.atlassian.com/jirakb/reset-a-user-s-login-count-from-the-database-in-jira-server-329352344.html)
 */
class ResetCaptcha private constructor(
    private val database: Database,
    private val username: String,
    private val sqlClient: SshSqlClient,
    private val schema: String
) : Database {
    override fun setup(ssh: SshConnection) = database.setup(ssh)

    override fun start(jira: URI, ssh: SshConnection) {
        database.start(jira, ssh)
        reset("login.totalFailedCount", ssh)
        reset("login.currentFailedCount", ssh)
    }

    private fun reset(attribute: String, ssh: SshConnection) {
        val sql = "UPDATE $schema.cwd_user_attributes SET attribute_value = '0'" +
            "WHERE user_id = (SELECT id FROM $schema.cwd_user WHERE user_name = '$username')" +
            "AND attribute_name = '$attribute';"
        sqlClient.runSql(ssh, sql)
    }

    class Builder(
        private var database: Database
    ) {
        private var sqlClient: SshSqlClient = SshMysqlClient()
        private var schema: String = "jiradb"
        private var username: String = "admin"

        fun database(database: Database) = apply { this.database = database }
        fun username(username: String) = apply { this.username = username }
        fun schema(schema: String) = apply { this.schema = schema }

        fun build() = ResetCaptcha(database, username, sqlClient, schema)
    }
}

fun Database.resetCaptcha(): Database = ResetCaptcha.Builder(this).build()
