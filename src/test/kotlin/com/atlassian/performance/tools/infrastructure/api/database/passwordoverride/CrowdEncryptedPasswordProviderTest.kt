package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.mock.MockSshSqlClient
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CrowdEncryptedPasswordProviderTest {
    private lateinit var sqlClient: MockSshSqlClient
    private lateinit var sshConnection: RememberingSshConnection
    private lateinit var tested: JiraUserEncryptedPasswordProvider
    private val passwordPlainText = "abcde"
    private val passwordEncrypted = "*****"

    @Before
    fun setup() {
        sqlClient = MockSshSqlClient()
        sshConnection = RememberingSshConnection()
        tested = CrowdEncryptedPasswordProvider(
            jiraDatabaseSchemaName = "jiradb",
            sqlClient = sqlClient,
            passwordPlainText = passwordPlainText,
            passwordEncryptedWithAtlassianSecurityPasswordEncoder = passwordEncrypted
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
        tested.getEncryptedPassword(sshConnection)
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
            tested.getEncryptedPassword(sshConnection)
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
        val password = tested.getEncryptedPassword(sshConnection)
        // then
        assertThat(password).isEqualTo(passwordEncrypted)
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
        val password = tested.getEncryptedPassword(sshConnection)
        // then
        assertThat(password).isEqualTo(passwordPlainText)
    }

}