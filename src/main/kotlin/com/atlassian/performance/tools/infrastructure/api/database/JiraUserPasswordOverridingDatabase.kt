package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI

data class JiraUserPassword(
    val plainText: String,
    val encrypted: String
)

/**
 * Based on https://confluence.atlassian.com/jira/retrieving-the-jira-administrator-192836.html
 *
 * To encode the password use [com.atlassian.crowd.password.encoder.AtlassianSecurityPasswordEncoder](https://docs.atlassian.com/atlassian-crowd/4.2.2/com/atlassian/crowd/password/encoder/AtlassianSecurityPasswordEncoder.html)
 * from the [com.atlassian.crowd.crowd-password-encoders](https://mvnrepository.com/artifact/com.atlassian.crowd/crowd-password-encoders/4.2.2).
 */
class JiraUserPasswordOverridingDatabase internal constructor(
    private val databaseDelegate: Database,
    private val sqlClient: SshSqlClient,
    private val username: String,
    private val userPassword: JiraUserPassword?,
    private val cwdUserTableName: String
) : Database {
    companion object {
        private val logger: Logger = LogManager.getLogger(JiraUserPasswordOverridingDatabase::class.java)
    }

    override fun performSetup(
        ssh: SshConnection
    ) = databaseDelegate.performSetup(ssh)

    override fun start(
        jira: URI,
        ssh: SshConnection
    ) {
        databaseDelegate.start(jira, ssh)
        if (userPassword == null) {
            logger.info("Password not provided, skipping dataset update")
        } else {
            if (shouldUseEncryption(ssh)) {
                logger.info("Updating credential with encrypted password")
                sqlClient.runSql(ssh, "UPDATE $cwdUserTableName SET credential='${userPassword.encrypted}' WHERE user_name='$username';")
            } else {
                logger.info("Updating credential with plain text password")
                sqlClient.runSql(ssh, "UPDATE $cwdUserTableName SET credential='${userPassword.plainText}' WHERE user_name='$username';")
            }
            logger.info("Password for user '$username' updated to '${userPassword.plainText}'")
        }
    }

    private fun shouldUseEncryption(ssh: SshConnection): Boolean {
        val dbNamePrefix = if (cwdUserTableName.contains(".")) cwdUserTableName.substringBefore(".") + "." else ""
        val sqlResult = sqlClient.runSql(ssh, "select attribute_value from ${dbNamePrefix}cwd_directory_attribute where attribute_name = 'user_encryption_method';").output
        return when  {
            sqlResult.contains("plaintext") -> false
            sqlResult.contains("atlassian-security") -> true
            else -> {
                logger.warn("Unknown user_encryption_method. Assuming encrypted password should be used")
                true
            }
        }
    }

    class Builder(
        private var databaseDelegate: Database,
        private var username: String,
        private var userPassword: JiraUserPassword?
    ) {
        private var sqlClient: SshSqlClient = SshMysqlClient()
        private var cwdUserTableName: String = "jiradb.cwd_user"

        fun databaseDelegate(databaseDelegate: Database) = apply { this.databaseDelegate = databaseDelegate }
        fun username(username: String) = apply { this.username = username }
        fun userPassword(userPassword: JiraUserPassword?) = apply { this.userPassword = userPassword }
        fun sqlClient(sqlClient: SshSqlClient) = apply { this.sqlClient = sqlClient }
        fun cwdUserTableName(cwdUserTableName: String) = apply { this.cwdUserTableName = cwdUserTableName }

        fun build() = JiraUserPasswordOverridingDatabase(
            databaseDelegate = databaseDelegate,
            sqlClient = sqlClient,
            username = username,
            userPassword = userPassword,
            cwdUserTableName = cwdUserTableName
        )
    }

}

fun Database.withAdminPassword(adminPassword: JiraUserPassword?) = JiraUserPasswordOverridingDatabase.Builder(
    databaseDelegate = this,
    username = "admin",
    userPassword = adminPassword
).build()