package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.infrastructure.mock.MockSshSqlClient
import com.atlassian.performance.tools.infrastructure.mock.RememberingDatabase
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URI

class JiraUserPasswordOverridingDatabaseTest {

    private val jira = URI("http://localhost/")
    private val samplePassword = "plain text password"
    private val expectedEncryptedPassword = "*******"

    private lateinit var database: Database
    private lateinit var underlyingDatabase: RememberingDatabase
    private lateinit var sshConnection: RememberingSshConnection
    private lateinit var sqlClient: MockSshSqlClient

    @Before
    fun setup() {
        underlyingDatabase = RememberingDatabase()
        sshConnection = RememberingSshConnection()
        sqlClient = MockSshSqlClient()
        database = JiraUserPasswordOverridingDatabase
            .Builder(
                databaseDelegate = underlyingDatabase,
                userPasswordPlainText = samplePassword,
                userPasswordEncryptorProvider = object : JiraUserPasswordEncryptorProvider {
                    override fun getEncryptor(ssh: SshConnection, sqlClient: SshSqlClient): JiraUserPasswordEncryptor {
                        return object : JiraUserPasswordEncryptor {
                            override fun getEncryptedPassword(plainTextPassword: String) = expectedEncryptedPassword
                        }
                    }
                }
            )
            .sqlClient(sqlClient)
            .jiraDatabaseSchemaName("jira")
            .build()
    }

    @Test
    fun shouldSetupUnderlyingDatabase() {
        // when
        database.setup(sshConnection)
        database.start(jira, sshConnection)
        // then
        assertThat(underlyingDatabase.isSetup)
            .`as`("underlying database setup")
            .isTrue()
    }

    @Test
    fun shouldStartUnderlyingDatabase() {
        // when
        database.setup(sshConnection)
        database.start(jira, sshConnection)
        // then
        assertThat(underlyingDatabase.isStarted)
            .`as`("underlying database started")
            .isTrue()
    }

    @Test
    fun shouldUpdatePassword() {
        // when
        database.setup(sshConnection)
        database.start(jira, sshConnection)
        // then
        assertThat(sqlClient.getLog())
            .`as`("sql queries executed")
            .containsExactly(
                "UPDATE jira.cwd_user SET credential='${expectedEncryptedPassword}' WHERE user_name='admin';"
            )
    }
}