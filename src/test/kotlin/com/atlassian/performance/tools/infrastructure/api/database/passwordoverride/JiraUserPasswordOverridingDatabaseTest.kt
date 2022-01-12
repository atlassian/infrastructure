package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.api.database.Database
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
    private val samplePlainTextPassword = "plain text password"
    private val expectedEncryptedPassword = "*******"

    data class TestContext(
        val database: Database,
        val underlyingDatabase: RememberingDatabase,
        val sshConnection: RememberingSshConnection,
        val sqlClient: MockSshSqlClient
    )

    fun setup(): TestContext {
        val db = RememberingDatabase()
        val sqlClient = MockSshSqlClient()
        return TestContext(
            underlyingDatabase = db,
            sshConnection = RememberingSshConnection(),
            sqlClient = sqlClient,
            database = db
                .overrideAdminPassword(
                    adminPasswordPlainText = samplePlainTextPassword,
                    adminPasswordEncrypted = expectedEncryptedPassword
                )
                .sqlClient(sqlClient)
                .schema("jira")
                .jiraUserEncryptedPasswordProvider(object : JiraUserEncryptedPasswordProvider {
                    override fun getEncryptedPassword(ssh: SshConnection) = expectedEncryptedPassword
                })
                .build()
        )
    }

    @Test
    fun shouldSetupUnderlyingDatabase() {
        val testContext = setup()
        with(testContext) {
            // when
            database.setup(sshConnection)
            database.start(jira, sshConnection)
            // then
            assertThat(underlyingDatabase.isSetup).`as`("underlying database setup").isTrue()
        }
    }

    @Test
    fun shouldStartUnderlyingDatabase() {
        val testContext = setup()
        with(testContext) {
            // when
            database.setup(sshConnection)
            database.start(jira, sshConnection)
            // then
            assertThat(underlyingDatabase.isStarted).`as`("underlying database started").isTrue()
        }
    }

    @Test
    fun shouldUpdatePassword() {
        val testContext = setup()
        with(testContext) {
            // when
            database.setup(sshConnection)
            database.start(jira, sshConnection)
            // then
            assertThat(sqlClient.getLog()).`as`("sql queries executed").containsExactly(
                "UPDATE jira.cwd_user SET credential='${expectedEncryptedPassword}' WHERE user_name='admin';"
            )
        }
    }
}