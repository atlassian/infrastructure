package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.api.database.JiraUserPasswordOverridingDatabase
import com.atlassian.performance.tools.infrastructure.mock.MockSshSqlClient
import com.atlassian.performance.tools.infrastructure.mock.RememberingDatabase
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URI

class JiraUserPasswordOverridingDatabaseTest {

    private val jira = URI("http://localhost/")
    private val samplePassword = "plain text password"
    private val passwordEncryptor = object : JiraUserPasswordEncryptor {
        override fun getEncryptedPassword(ssh: SshConnection): String {
            return samplePassword
        }
    }

    @Test
    fun shouldSetupUnderlyingDatabase() {
        val underlyingDatabase = RememberingDatabase()
        val database = JiraUserPasswordOverridingDatabase.Builder(
            databaseDelegate = underlyingDatabase,
            userPasswordPlainText = samplePassword,
            jiraUserPasswordEncryptor = passwordEncryptor
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
            userPasswordPlainText = samplePassword,
            jiraUserPasswordEncryptor = passwordEncryptor
        ).build()
        val sshConnection = RememberingSshConnection()

        database.setup(sshConnection)
        database.start(jira, sshConnection)

        assertThat(underlyingDatabase.isStarted)
            .`as`("underlying database started")
            .isTrue()
    }

    @Test
    fun shouldUpdatedPassword() {
        // given
        val underlyingDatabase = RememberingDatabase()
        val sqlClient = MockSshSqlClient()
        val database = JiraUserPasswordOverridingDatabase(
            databaseDelegate = underlyingDatabase,
            sqlClient = sqlClient,
            username = "admin",
            userPasswordPlainText = samplePassword,
            jiraDatabaseSchemaName = "jira",
            jiraUserPasswordEncryptor = passwordEncryptor
        )
        val sshConnection = RememberingSshConnection()

        // when
        database.setup(sshConnection)
        database.start(jira, sshConnection)

        // then
        assertThat(sqlClient.getLog())
            .`as`("sql queries executed")
            .containsExactly(
                "UPDATE jira.cwd_user SET credential='${samplePassword}' WHERE user_name='admin';"
            )
    }
}