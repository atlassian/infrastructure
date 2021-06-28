package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.mock.RememberingDatabase
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import com.atlassian.performance.tools.infrastructure.mock.RememberingSshSqlClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URI

class JiraUserPasswordOverridingDatabaseTest {

    private val jira = URI("http://localhost/")
    private val exampleEncodedPassword = "uQieO/1CGMUIXXftw3ynrsaYLShI+GTcPS4LdUGWbIusFvHPfUzD7CZvms6yMMvA8I7FViHVEqr6Mj4pCLKAFQ=="

    @Test
    fun shouldSetupUnderlyingDatabase() {
        val underlyingDatabase = RememberingDatabase()
        val database = JiraUserPasswordOverridingDatabase.Builder(
            databaseDelegate = underlyingDatabase,
            username = "admin",
            encodedPassword = exampleEncodedPassword
        ).build()
        val sshConnection = RememberingSshConnection()

        database.setup(sshConnection)
        database.start(jira, sshConnection)

        assertThat(underlyingDatabase.setup)
            .`as`("underlying database setup")
            .isTrue()
    }

    @Test
    fun shouldStartUnderlyingDatabase() {
        val underlyingDatabase = RememberingDatabase()
        val database = JiraUserPasswordOverridingDatabase.Builder(
            databaseDelegate = underlyingDatabase,
            username = "admin",
            encodedPassword = exampleEncodedPassword
        ).build()
        val sshConnection = RememberingSshConnection()

        database.setup(sshConnection)
        database.start(jira, sshConnection)

        assertThat(underlyingDatabase.started)
            .`as`("underlying database started")
            .isTrue()
    }

    @Test
    fun shouldExecuteUpdateOnCwdUserTable() {
        val cwdUserTableName = "cwd_user"
        val underlyingDatabase = RememberingDatabase()
        val sqlClient = RememberingSshSqlClient()
        val database = JiraUserPasswordOverridingDatabase(
            databaseDelegate = underlyingDatabase,
            sqlClient = sqlClient,
            username = "admin",
            encodedPassword = exampleEncodedPassword,
            cwdUserTableName = cwdUserTableName
        )
        val sshConnection = RememberingSshConnection()

        database.setup(sshConnection)
        database.start(jira, sshConnection)

        assertThat(sqlClient.getLog())
            .`as`("sql command executed")
            .contains("UPDATE $cwdUserTableName SET credential='$exampleEncodedPassword' WHERE user_name='admin';")
    }
}