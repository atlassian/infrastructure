package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.database.passwordoverride.DefaultJiraUserPasswordEncryptor
import com.atlassian.performance.tools.infrastructure.api.database.passwordoverride.JiraUserPasswordEncryptor
import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI

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
    private val jiraDatabaseSchemaName: String,
    private val userPasswordPlainText: String,
    private val jiraUserPasswordEncryptor: JiraUserPasswordEncryptor
) : Database {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun setup(ssh: SshConnection): String = databaseDelegate.setup(ssh)

    override fun start(
        jira: URI,
        ssh: SshConnection
    ) {
        databaseDelegate.start(jira, ssh)
        val password = jiraUserPasswordEncryptor.getEncryptedPassword(ssh)
        sqlClient.runSql(ssh, "UPDATE ${jiraDatabaseSchemaName}.cwd_user SET credential='$password' WHERE user_name='$username';")
        logger.debug("Password for user '$username' updated to '${userPasswordPlainText}'")
    }


    class Builder(
        private var databaseDelegate: Database,
        private var userPasswordPlainText: String,
        private var jiraUserPasswordEncryptor: JiraUserPasswordEncryptor
    ) {
        private var sqlClient: SshSqlClient = SshMysqlClient()
        private var jiraDatabaseSchemaName: String = "jiradb"
        private var username: String = "admin"

        fun databaseDelegate(databaseDelegate: Database) = apply { this.databaseDelegate = databaseDelegate }
        fun username(username: String) = apply { this.username = username }
        fun userPasswordPlainText(userPassword: String) = apply { this.userPasswordPlainText = userPassword }
        fun sqlClient(sqlClient: SshSqlClient) = apply { this.sqlClient = sqlClient }
        fun jiraDatabaseSchemaName(jiraDatabaseSchemaName: String) = apply { this.jiraDatabaseSchemaName = jiraDatabaseSchemaName }
        fun jiraUserPasswordEncryptor(jiraUserPasswordEncryptor: JiraUserPasswordEncryptor) = apply { this.jiraUserPasswordEncryptor = jiraUserPasswordEncryptor }

        fun build() = JiraUserPasswordOverridingDatabase(
            databaseDelegate = databaseDelegate,
            sqlClient = sqlClient,
            username = username,
            userPasswordPlainText = userPasswordPlainText,
            jiraDatabaseSchemaName = jiraDatabaseSchemaName,
            jiraUserPasswordEncryptor = jiraUserPasswordEncryptor
        )
    }

}

fun Database.withAdminPassword(adminPasswordPlainText: String, passwordEncryptFunction: (String) -> String): JiraUserPasswordOverridingDatabase {
    val jiraDatabaseSchemaName = "jiradb"
    val sqlClient = SshMysqlClient()
    return JiraUserPasswordOverridingDatabase.Builder(
        databaseDelegate = this,
        userPasswordPlainText = adminPasswordPlainText,
        jiraUserPasswordEncryptor = DefaultJiraUserPasswordEncryptor(
            passwordEncryptFunction = passwordEncryptFunction,
            userPasswordPlainText = adminPasswordPlainText,
            sqlClient = sqlClient,
            jiraDatabaseSchemaName = jiraDatabaseSchemaName
        )
    )
        .jiraDatabaseSchemaName(jiraDatabaseSchemaName)
        .sqlClient(sqlClient)
        .build()
}