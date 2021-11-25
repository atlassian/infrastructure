package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.mock.MockSshSqlClient
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class DefaultJiraUserPasswordEncryptorTest {
    private val plainTextPassword = "plain text password"
    private val encryptedPassword = "***** ***"

    private lateinit var sqlClient: MockSshSqlClient
    private lateinit var sshConnection: RememberingSshConnection
    private lateinit var jiraUserPasswordEncryptor: JiraUserPasswordEncryptor

    @Before
    fun setup() {
        sqlClient = MockSshSqlClient()
        sshConnection = RememberingSshConnection()
        jiraUserPasswordEncryptor = DefaultJiraUserPasswordEncryptor(
            passwordEncryptFunction = { _ -> encryptedPassword },
            userPasswordPlainText = plainTextPassword,
            sqlClient = sqlClient,
            jiraDatabaseSchemaName = "jiradb"
        )
    }

    @Test
    fun shouldCQueryEncryptionMethod() {
        // when
        jiraUserPasswordEncryptor.getEncryptedPassword(sshConnection)
        // then
        assertThat(sqlClient.getLog())
            .`as`("sql queries executed")
            .containsExactly(
                "select attribute_value from jiradb.cwd_directory_attribute where attribute_name = 'user_encryption_method';"
            )
    }

    @Test
    fun shouldReturnEncryptedPasswordByDefault() {
        // when
        val encryptedPassword = jiraUserPasswordEncryptor.getEncryptedPassword(sshConnection)
        // then
        assertThat(encryptedPassword).isEqualTo(encryptedPassword)
    }

    @Test
    fun shouldUpdateEncryptedPassword() {
        // given
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
        val encryptedPassword = jiraUserPasswordEncryptor.getEncryptedPassword(sshConnection)
        // then
        assertThat(encryptedPassword).isEqualTo(encryptedPassword)
    }

    @Test
    fun shouldUpdatePlaintextPassword() {
        // given
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
        val encryptedPassword = jiraUserPasswordEncryptor.getEncryptedPassword(sshConnection)
        // then
        assertThat(encryptedPassword).isEqualTo(plainTextPassword)
    }

}