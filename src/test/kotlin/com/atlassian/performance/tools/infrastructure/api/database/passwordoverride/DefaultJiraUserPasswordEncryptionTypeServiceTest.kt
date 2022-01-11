package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.mock.MockSshSqlClient
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.function.Function

class DefaultJiraUserPasswordEncryptionTypeServiceTest {
    private lateinit var sqlClient: MockSshSqlClient
    private lateinit var sshConnection: RememberingSshConnection
    private lateinit var tested: JiraUserPasswordEncryptorProvider
    private val plainTextPasswordEncryptor: JiraUserPasswordEncryptor = PlainTextJiraUserPasswordEncryptor()
    private val encryptedPasswordEncryptor: JiraUserPasswordEncryptor = EncryptedJiraUserPasswordEncryptor(Function { "" })

    @Before
    fun setup() {
        sqlClient = MockSshSqlClient()
        sshConnection = RememberingSshConnection()
        tested = DefaultJiraUserPasswordEncryptorProvider(
            jiraDatabaseSchemaName = "jiradb",
            plainTextPasswordEncryptor = plainTextPasswordEncryptor,
            encryptedPasswordEncryptor = encryptedPasswordEncryptor
        )
    }

    @Test
    fun shouldQueryEncryptionMethod() {
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
        tested.getEncryptor(sshConnection, sqlClient)
        // then
        assertThat(sqlClient.getLog())
            .`as`("sql queries executed")
            .containsExactly(
                "select attribute_value from jiradb.cwd_directory_attribute where attribute_name = 'user_encryption_method';"
            )
    }

    @Test
    fun shouldThrowExceptionWhenUnknownEncryption() {
        // when
        var exception: RuntimeException? = null
        try {
            tested.getEncryptor(sshConnection, sqlClient)
        } catch (e: RuntimeException) {
            exception = e
        }
        // then
        assertThat(exception).isNotNull()
        assertThat(exception!!.message).isEqualTo("Unknown jira user password encryption type")
    }

    @Test
    fun shouldReturnEncrypted() {
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
        val passwordEncryptor = tested.getEncryptor(sshConnection, sqlClient)
        // then
        assertThat(passwordEncryptor).isEqualTo(encryptedPasswordEncryptor)
    }

    @Test
    fun shouldReturnPlainText() {
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
        val passwordEncryptor = tested.getEncryptor(sshConnection, sqlClient)
        // then
        assertThat(passwordEncryptor).isEqualTo(plainTextPasswordEncryptor)
    }

}