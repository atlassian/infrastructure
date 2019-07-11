package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.mock.ExecutionRememberingSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.net.URI

class LicenseOverridingMysqlTest {

    private val jira = URI("http://localhost/")

    @Test
    fun shouldOverrideOneLicense() {
        val licenseStrings = listOf("the only license")
        val (testedDatabase, underlyingDatabase, ssh) = setUp(licenseStrings)

        testedDatabase.start(jira, ssh)

        assertThat(underlyingDatabase.started)
            .`as`("underlying database started")
            .isTrue()

        assertSshCommands(
            ssh,
            """mysql -h 127.0.0.1 -u root -e "DELETE FROM jiradb.productlicense;"""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[0].remoteDestination}""",
            """rm ${ssh.uploads[0].remoteDestination}"""
        )

        assertSshUploads(ssh, licenseStrings)
    }

    @Test
    fun shouldOverrideTwoLicenses() {
        val licenseStrings = listOf("the first license", "the second license")
        val (testedDatabase, _, ssh) = setUp(licenseStrings)

        testedDatabase.start(jira, ssh)

        assertSshCommands(
            ssh,
            """mysql -h 127.0.0.1 -u root -e "DELETE FROM jiradb.productlicense;"""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[0].remoteDestination}""",
            """rm ${ssh.uploads[0].remoteDestination}""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[1].remoteDestination}""",
            """rm ${ssh.uploads[1].remoteDestination}"""
        )

        assertSshUploads(ssh, licenseStrings)
    }

    @Test
    fun shouldOverrideThreeLicenses() {
        val licenseStrings = listOf("the first license", "the second license", "the third license")
        val (testedDatabase, _, ssh) = setUp(licenseStrings)

        testedDatabase.start(jira, ssh)

        assertSshCommands(
            ssh,
            """mysql -h 127.0.0.1 -u root -e "DELETE FROM jiradb.productlicense;"""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[0].remoteDestination}""",
            """rm ${ssh.uploads[0].remoteDestination}""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[1].remoteDestination}""",
            """rm ${ssh.uploads[1].remoteDestination}""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[2].remoteDestination}""",
            """rm ${ssh.uploads[2].remoteDestination}"""
        )

        assertSshUploads(ssh, licenseStrings)
    }

    @Test
    fun shouldOverrideThreeLicensesFromFilesUsingBuilder() {
        val licenseStrings = listOf("the first license", "the second license", "the third license")

        val licenseFiles = licenseStrings.map { createTempLicenseFile(it) }

        val (testedDatabase, _, ssh) = setUpWithLicenseFiles(licenseFiles)

        testedDatabase.start(jira, ssh)

        assertSshCommands(
            ssh,
            """mysql -h 127.0.0.1 -u root -e "DELETE FROM jiradb.productlicense;"""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[0].remoteDestination}""",
            """rm ${ssh.uploads[0].remoteDestination}""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[1].remoteDestination}""",
            """rm ${ssh.uploads[1].remoteDestination}""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[2].remoteDestination}""",
            """rm ${ssh.uploads[2].remoteDestination}"""
        )

        assertSshUploads(ssh, licenseStrings)
    }

    @Test
    fun shouldOverrideThreeLicensesFromStringUsingBuilder() {
        val licenseStrings = listOf("the first license", "the second license", "the third license")

        val (testedDatabase, _, ssh) = setUpWithLicenseStrings(licenseStrings)

        testedDatabase.start(jira, ssh)

        assertSshCommands(
            ssh,
            """mysql -h 127.0.0.1 -u root -e "DELETE FROM jiradb.productlicense;"""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[0].remoteDestination}""",
            """rm ${ssh.uploads[0].remoteDestination}""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[1].remoteDestination}""",
            """rm ${ssh.uploads[1].remoteDestination}""",
            """mysql -h 127.0.0.1 -u root < ${ssh.uploads[2].remoteDestination}""",
            """rm ${ssh.uploads[2].remoteDestination}"""
        )

        assertSshUploads(ssh, licenseStrings)
    }

    private fun setUp(
        licenses: List<String>
    ): DatabaseStartTest {
        val underlyingDatabase = RememberingDatabase()
        @Suppress("DEPRECATION")
        return DatabaseStartTest(
            testedDatabase = LicenseOverridingMysql(
                database = underlyingDatabase,
                licenses = licenses
            ),
            underlyingDatabase = underlyingDatabase,
            ssh = ExecutionRememberingSshConnection()
        )
    }

    private fun setUpWithLicenseFiles(
        licenses: List<File>
    ): DatabaseStartTest {
        val underlyingDatabase = RememberingDatabase()
        return DatabaseStartTest(
            testedDatabase = LicenseOverridingMysql
                .Builder(underlyingDatabase)
                .licenseFiles(licenses)
                .build(),
            underlyingDatabase = underlyingDatabase,
            ssh = ExecutionRememberingSshConnection()
        )
    }

    private fun setUpWithLicenseStrings(
        licenses: List<String>
    ): DatabaseStartTest {
        val underlyingDatabase = RememberingDatabase()
        @Suppress("DEPRECATION")
        return DatabaseStartTest(
            testedDatabase = LicenseOverridingMysql
                .Builder(underlyingDatabase)
                .licenseStrings(licenses)
                .build(),
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

    private fun assertSshUploads(ssh: ExecutionRememberingSshConnection, licenseStrings: List<String>) {

        assertThat(ssh.uploads.map { it.content })
            .`as`("SSH upload content")
            .containsExactly(*licenseStrings
                .mapIndexed { i, c ->
                    """INSERT INTO jiradb.productlicense VALUES ($i, "$c");""" }
                .toTypedArray())

        ssh.uploads.forEach {
            assertThat(it.remoteDestination)
                .`as`("SSH upload files")
                .isEqualTo(it.localSource.name)
        }
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
}
