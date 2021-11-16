package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.mock.RememberingDatabase
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshSqlClient
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
            username = "admin",
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
            username = "admin",
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
    fun shouldExecuteUpdateOnCwdUserTable() {
        // given
        val cwdUserTableName = "cwd_user"
        val underlyingDatabase = RememberingDatabase()
        val sqlClient = RememberingSshSqlClient()
        val database = JiraUserPasswordOverridingDatabase(
            databaseDelegate = underlyingDatabase,
            sqlClient = sqlClient,
            username = "admin",
            userPassword = samplePassword,
            cwdUserTableName = cwdUserTableName
        )
        val sshConnection = RememberingSshConnection()

        // when
        database.setup(sshConnection)
        database.start(jira, sshConnection)

        // then
        assertThat(sqlClient.getLog())
            .`as`("sql command executed")
            .contains("UPDATE $cwdUserTableName SET credential='${samplePassword.encrypted}' WHERE user_name='admin';")
    }
}