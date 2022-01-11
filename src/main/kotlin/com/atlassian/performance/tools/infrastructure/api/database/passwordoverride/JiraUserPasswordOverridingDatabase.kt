package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI

class JiraUserPasswordOverridingDatabase private constructor(
    private val databaseDelegate: Database,
    private val sqlClient: SshSqlClient,
    private val username: String,
    private val schema: String,
    private val userPasswordPlainText: String,
    private val jiraUserEncryptedPasswordProvider: JiraUserEncryptedPasswordProvider
) : Database {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun setup(ssh: SshConnection): String = databaseDelegate.setup(ssh)

    override fun start(
        jira: URI,
        ssh: SshConnection
    ) {
        databaseDelegate.start(jira, ssh)
        val password = jiraUserEncryptedPasswordProvider.getEncryptedPassword(ssh)
        sqlClient.runSql(ssh, "UPDATE ${schema}.cwd_user SET credential='$password' WHERE user_name='$username';")
        logger.debug("Password for user '$username' updated to '${userPasswordPlainText}'")
    }


    class Builder(
        private var databaseDelegate: Database,
        private var plainTextPassword: String,
        private var passwordEncrypted: String
    ) {
        private var sqlClient: SshSqlClient = SshMysqlClient()
        private var schema: String = "jiradb"
        private var username: String = "admin"
        private var jiraUserEncryptedPasswordProvider: JiraUserEncryptedPasswordProvider? = null

        fun databaseDelegate(databaseDelegate: Database) = apply { this.databaseDelegate = databaseDelegate }
        fun username(username: String) = apply { this.username = username }
        fun plainTextPassword(userPasswordPlainText: String) = apply { this.plainTextPassword = userPasswordPlainText }
        fun passwordEncrypted(userPasswordEncrypted: String) = apply { this.passwordEncrypted = userPasswordEncrypted }
        fun sqlClient(sqlClient: SshSqlClient) = apply { this.sqlClient = sqlClient }
        fun schema(jiraDatabaseSchemaName: String) = apply { this.schema = jiraDatabaseSchemaName }
        fun jiraUserEncryptedPasswordProvider(jiraUserEncryptedPasswordProvider: JiraUserEncryptedPasswordProvider) =
            apply { this.jiraUserEncryptedPasswordProvider = jiraUserEncryptedPasswordProvider }

        fun build() = JiraUserPasswordOverridingDatabase(
            databaseDelegate = databaseDelegate,
            sqlClient = sqlClient,
            username = username,
            userPasswordPlainText = plainTextPassword,
            schema = schema,
            jiraUserEncryptedPasswordProvider = jiraUserEncryptedPasswordProvider ?: CrowdEncryptedPasswordProvider(
                jiraDatabaseSchemaName = schema,
                passwordPlainText = plainTextPassword,
                passwordEncryptedWithAtlassianSecurityPasswordEncoder = passwordEncrypted,
                sqlClient = sqlClient
            )
        )
    }

}

/**
 * @param adminPasswordEncrypted Based on [retrieving-the-jira-administrator](https://confluence.atlassian.com/jira/retrieving-the-jira-administrator-192836.html)
 * to encode the password in Jira format use [com.atlassian.crowd.password.encoder.AtlassianSecurityPasswordEncoder](https://docs.atlassian.com/atlassian-crowd/4.2.2/com/atlassian/crowd/password/encoder/AtlassianSecurityPasswordEncoder.html)
 * from the [com.atlassian.crowd.crowd-password-encoders](https://mvnrepository.com/artifact/com.atlassian.crowd/crowd-password-encoders/4.2.2).
 *
 */
fun Database.overrideAdminPassword(adminPasswordPlainText: String, adminPasswordEncrypted: String): JiraUserPasswordOverridingDatabase.Builder {
    return JiraUserPasswordOverridingDatabase.Builder(
        databaseDelegate = this,
        plainTextPassword = adminPasswordPlainText,
        passwordEncrypted = adminPasswordEncrypted
    )
}
