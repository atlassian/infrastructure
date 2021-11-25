package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.mock.MockSshSqlClient
import com.atlassian.performance.tools.infrastructure.mock.RememberingDatabase
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URI

class JiraUserPasswordOverridingDatabaseTest {

    private val jira = URI("http://localhost/")
    private val samplePassword = JiraUserPassword(
        plainText = "**plain text**",
        encrypted = "**encrypted**"
    )

    @Test
    fun shouldSetupUnderlyingDatabase() {
        val underlyingDatabase = RememberingDatabase()
        val database = JiraUserPasswordOverridingDatabase.Builder(
            databaseDelegate = underlyingDatabase,
            userPassword = samplePassword
        ).build()
        val sshConnection = RememberingSshConnection()

        database.setup(sshConnection)
        database.start(jira, sshConnection)

        assertThat(underlyingDatabase.isSetup)
            .`as`("underlying database setup")
            .isTrue()
    }

    @Test
    fun shouldStartUnderlyingDatabase() {
        val underlyingDatabase = RememberingDatabase()
        val database = JiraUserPasswordOverridingDatabase.Builder(
            databaseDelegate = underlyingDatabase,
            userPassword = samplePassword
        ).build()
        val sshConnection = RememberingSshConnection()

        database.setup(sshConnection)
        database.start(jira, sshConnection)

        assertThat(underlyingDatabase.isStarted)
            .`as`("underlying database started")
            .isTrue()
    }

    @Test
    fun shouldUpdateEncryptedPasswordByDefault() {
        // given
        val underlyingDatabase = RememberingDatabase()
        val sqlClient = MockSshSqlClient()
        val database = JiraUserPasswordOverridingDatabase(
            databaseDelegate = underlyingDatabase,
            sqlClient = sqlClient,
            username = "admin",
            userPassword = samplePassword,
            jiraDatabaseSchemaName = "jira"
        )
        val sshConnection = RememberingSshConnection()

        // when
        database.setup(sshConnection)
        database.start(jira, sshConnection)

        // then
        assertThat(sqlClient.getLog())
            .`as`("sql queries executed")
            .containsExactly(
                "select attribute_value from jira.cwd_directory_attribute where attribute_name = 'user_encryption_method';",
                "UPDATE jira.cwd_user SET credential='${samplePassword.encrypted}' WHERE user_name='admin';"
            )
    }

    @Test
    fun shouldUpdateEncryptedPassword() {
        // given
        val underlyingDatabase = RememberingDatabase()
        val sqlClient = MockSshSqlClient()
        val database = JiraUserPasswordOverridingDatabase(
            databaseDelegate = underlyingDatabase,
            sqlClient = sqlClient,
            username = "admin",
            userPassword = samplePassword,
            jiraDatabaseSchemaName = "jiradb"
        )
        val sshConnection = RememberingSshConnection()
        sqlClient.queueReturnedSqlCommandResult(
            SshConnection.SshResult(
                exitStatus = 0,
                output = """attribute_value
                            atlassian-security
""".trimMargin(),
                errorOutput = ""
            )
        )

        // when
        database.setup(sshConnection)
        database.start(jira, sshConnection)

        // then
        assertThat(sqlClient.getLog())
            .`as`("sql queries executed")
            .containsExactly(
                "select attribute_value from jiradb.cwd_directory_attribute where attribute_name = 'user_encryption_method';",
                "UPDATE jiradb.cwd_user SET credential='${samplePassword.encrypted}' WHERE user_name='admin';"
            )
    }

    @Test
    fun shouldUpdatePlaintextPassword() {
        // given
        val underlyingDatabase = RememberingDatabase()
        val sqlClient = MockSshSqlClient()
        val database = JiraUserPasswordOverridingDatabase(
            databaseDelegate = underlyingDatabase,
            sqlClient = sqlClient,
            username = "admin",
            userPassword = samplePassword,
            jiraDatabaseSchemaName = "jira"
        )
        val sshConnection = RememberingSshConnection()
        sqlClient.queueReturnedSqlCommandResult(
            SshConnection.SshResult(
                exitStatus = 0,
                output = """attribute_value
                            plaintext
""".trimMargin(),
                errorOutput = ""
            )
        )

        // when
        database.setup(sshConnection)
        database.start(jira, sshConnection)

        // then
        assertThat(sqlClient.getLog())
            .`as`("sql queries executed")
            .containsExactly(
                "select attribute_value from jira.cwd_directory_attribute where attribute_name = 'user_encryption_method';",
                "UPDATE jira.cwd_user SET credential='${samplePassword.plainText}' WHERE user_name='admin';"
            )
    }
}