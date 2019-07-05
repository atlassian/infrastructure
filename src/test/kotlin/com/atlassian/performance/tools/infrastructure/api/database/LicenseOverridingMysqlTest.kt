package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.mock.UnimplementedSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection.SshResult
import org.apache.logging.log4j.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URI
import java.time.Duration

class LicenseOverridingMysqlTest {

    private val jira = URI("http://localhost/")

    @Test
    fun shouldOverrideOneLicense() {
        val (testedDatabase, underlyingDatabase, ssh) = setUp(listOf("the only license"))

        testedDatabase.start(jira, ssh)

        assertThat(underlyingDatabase.started)
            .`as`("underlying database started")
            .isTrue()
        assertSshCommands(
            ssh,
            """mysql -h 127.0.0.1 -u root -e "DELETE FROM jiradb.productlicense; REPLACE INTO jiradb.productlicense (LICENSE) VALUES (\"the only license\");""""
        )
    }

    @Test
    fun shouldOverrideTwoLicenses() {
        val (testedDatabase, _, ssh) = setUp(listOf("the first license", "the second license"))

        testedDatabase.start(jira, ssh)

        assertSshCommands(
            ssh,
            """mysql -h 127.0.0.1 -u root -e "DELETE FROM jiradb.productlicense; REPLACE INTO jiradb.productlicense (LICENSE) VALUES (\"the first license\");"""",
            """mysql -h 127.0.0.1 -u root -e "INSERT INTO jiradb.productlicense SELECT MAX(id)+1, \"the second license\" FROM jiradb.productlicense;""""
        )
    }

    @Test
    fun shouldOverrideThreeLicenses() {
        val (testedDatabase, _, ssh) = setUp(listOf("the first license", "the second license", "the third license"))

        testedDatabase.start(jira, ssh)

        assertSshCommands(
            ssh,
            """mysql -h 127.0.0.1 -u root -e "DELETE FROM jiradb.productlicense; REPLACE INTO jiradb.productlicense (LICENSE) VALUES (\"the first license\");"""",
            """mysql -h 127.0.0.1 -u root -e "INSERT INTO jiradb.productlicense SELECT MAX(id)+1, \"the second license\" FROM jiradb.productlicense;"""",
            """mysql -h 127.0.0.1 -u root -e "INSERT INTO jiradb.productlicense SELECT MAX(id)+1, \"the third license\" FROM jiradb.productlicense;""""
        )
    }

    private fun setUp(
        licenses: List<String>
    ): DatabaseStartTest {
        val underlyingDatabase = RememberingDatabase()
        return DatabaseStartTest(
            testedDatabase = LicenseOverridingMysql(
                database = underlyingDatabase,
                licenses = licenses
            ),
            underlyingDatabase = underlyingDatabase,
            ssh = ExecutionRememberingSshConnection()
        )
    }

    private fun assertSshCommands(
        ssh: ExecutionRememberingSshConnection,
        vararg expectedSshCommands: String
    ) {
        assertThat(ssh.commands)
            .`as`("SSH commands")
            .containsExactly(*expectedSshCommands)
    }

    private data class DatabaseStartTest(
        val testedDatabase: LicenseOverridingMysql,
        val underlyingDatabase: RememberingDatabase,
        val ssh: ExecutionRememberingSshConnection
    )

    private class RememberingDatabase : Database {

        var setup = false
        var started = false

        override fun setup(ssh: SshConnection): String {
            setup = true
            return "."
        }

        override fun start(jira: URI, ssh: SshConnection) {
            started = true
        }
    }

    private class ExecutionRememberingSshConnection : SshConnection by UnimplementedSshConnection() {

        val commands = mutableListOf<String>()

        override fun execute(
            cmd: String,
            timeout: Duration,
            stdout: Level,
            stderr: Level
        ): SshResult {
            commands.add(cmd)
            return SshResult(
                exitStatus = 0,
                output = "",
                errorOutput = ""
            )
        }
    }
}
