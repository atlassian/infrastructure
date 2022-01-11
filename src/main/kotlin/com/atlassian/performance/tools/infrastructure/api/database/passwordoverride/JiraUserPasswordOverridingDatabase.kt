package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.util.function.Function

class JiraUserPasswordOverridingDatabase internal constructor(
    private val databaseDelegate: Database,
    private val sqlClient: SshSqlClient,
    private val username: String,
    private val jiraDatabaseSchemaName: String,
    private val userPasswordPlainText: String,
    private val userPasswordEncryptorProvider: JiraUserPasswordEncryptorProvider
) : Database {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun setup(ssh: SshConnection): String = databaseDelegate.setup(ssh)

    override fun start(
        jira: URI,
        ssh: SshConnection
    ) {
        databaseDelegate.start(jira, ssh)
        val userPasswordEncryptor = userPasswordEncryptorProvider.getEncryptor(ssh, sqlClient)
        val password = userPasswordEncryptor.getEncryptedPassword(userPasswordPlainText)
        sqlClient.runSql(ssh, "UPDATE ${jiraDatabaseSchemaName}.cwd_user SET credential='$password' WHERE user_name='$username';")
        logger.debug("Password for user '$username' updated to '${userPasswordPlainText}'")
    }


    class Builder(
        private var databaseDelegate: Database,
        private var userPasswordPlainText: String,
        private var userPasswordEncryptorProvider: JiraUserPasswordEncryptorProvider
    ) {
        private var sqlClient: SshSqlClient = SshMysqlClient()
        private var jiraDatabaseSchemaName: String = "jiradb"
        private var username: String = "admin"

        fun databaseDelegate(databaseDelegate: Database) = apply { this.databaseDelegate = databaseDelegate }
        fun username(username: String) = apply { this.username = username }
        fun userPasswordPlainText(userPassword: String) = apply { this.userPasswordPlainText = userPassword }
        fun sqlClient(sqlClient: SshSqlClient) = apply { this.sqlClient = sqlClient }
        fun jiraDatabaseSchemaName(jiraDatabaseSchemaName: String) = apply { this.jiraDatabaseSchemaName = jiraDatabaseSchemaName }
        fun userPasswordEncryptorProvider(userPasswordEncryptorProvider: JiraUserPasswordEncryptorProvider) =
            apply { this.userPasswordEncryptorProvider = userPasswordEncryptorProvider }

        fun build() = JiraUserPasswordOverridingDatabase(
            databaseDelegate = databaseDelegate,
            sqlClient = sqlClient,
            username = username,
            userPasswordPlainText = userPasswordPlainText,
            jiraDatabaseSchemaName = jiraDatabaseSchemaName,
            userPasswordEncryptorProvider = userPasswordEncryptorProvider
        )
    }

}

/**
 * @param passwordEncryptFunction Based on [retrieving-the-jira-administrator](https://confluence.atlassian.com/jira/retrieving-the-jira-administrator-192836.html)
 * to encode the password in Jira format use [com.atlassian.crowd.password.encoder.AtlassianSecurityPasswordEncoder](https://docs.atlassian.com/atlassian-crowd/4.2.2/com/atlassian/crowd/password/encoder/AtlassianSecurityPasswordEncoder.html)
 * from the [com.atlassian.crowd.crowd-password-encoders](https://mvnrepository.com/artifact/com.atlassian.crowd/crowd-password-encoders/4.2.2).
 *
 */
fun Database.withAdminPassword(adminPasswordPlainText: String, passwordEncryptFunction: Function<String, String>): Database {
    val jiraDatabaseSchemaName = "jiradb"
    val sqlClient = SshMysqlClient()
    return JiraUserPasswordOverridingDatabase.Builder(
        databaseDelegate = this,
        userPasswordPlainText = adminPasswordPlainText,
        userPasswordEncryptorProvider = DefaultJiraUserPasswordEncryptorProvider(
            jiraDatabaseSchemaName = jiraDatabaseSchemaName,
            plainTextPasswordEncryptor = PlainTextJiraUserPasswordEncryptor(),
            encryptedPasswordEncryptor = EncryptedJiraUserPasswordEncryptor(passwordEncryptFunction)
        )
    )
        .jiraDatabaseSchemaName(jiraDatabaseSchemaName)
        .sqlClient(sqlClient)
        .build()
}