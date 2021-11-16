package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.net.URI
import java.nio.file.Files


/**
 * Removes all licenses from [database] and adds [licenses] instead.
 *
 * @param [database] must be MySQL
 * @since 4.13.0
 */
class LicenseOverridingMysql private constructor(
    private val database: Database,
    private val licenseCollection: LicenseCollection
) : Database {
    companion object {
        private val logger: Logger = LogManager.getLogger(LicenseOverridingMysql::class.java)
    }

    @Deprecated(message = "Use the Builder and pass licenses as Files to reduce accidental leakage of the license")
    constructor(
        database: Database,
        licenses: List<String>
    ) : this(database, LicenseCollection(licenses.map<String, File> {
        createTempLicenseFile(it)
    }))

    override fun setup(
        ssh: SshConnection
    ): DatabaseSetup = database.setup(ssh)

    override fun start(
        jira: URI,
        ssh: SshConnection
    ) {
        database.start(jira, ssh)
        val licenseTable = "jiradb.productlicense"
        val client = SshMysqlClient()
        client.runSql(ssh, "DELETE FROM $licenseTable;")
        logger.info("Licenses nuked")
        licenseCollection.licenses.forEachIndexed { index, license ->
            val flatLicenseText = license.readLines().joinToString(separator = "") { it.trim() }
            val insert = "INSERT INTO $licenseTable VALUES ($index, \"$flatLicenseText\");"
            val insertFile = Files.createTempFile("license-insert", ".sql").toFile()
            insertFile.deleteOnExit()
            insertFile
                .bufferedWriter()
                .use { it.write(insert) }
            client.runSql(ssh, insertFile)
            insertFile.delete()
            logger.info("Added license: ${flatLicenseText.substring(0..8)}...")
        }
    }

    private class LicenseCollection(
        val licenses: List<File>
    )

    class Builder(private val database: Database) {
        private var licenseFiles: List<File> = emptyList()

        @Deprecated(message = "Pass licenses as Files to reduce accidental leakage of the license")
        fun licenseStrings(licenses: List<String>) = apply {
            this.licenseFiles = licenses.map {
                createTempLicenseFile(it)
            }
        }

        fun licenseFiles(licenses: List<File>) = apply { this.licenseFiles = licenses }

        fun build(): LicenseOverridingMysql {
            return LicenseOverridingMysql(
                database = database,
                licenseCollection = LicenseCollection(licenseFiles)
            )
        }
    }
}

internal fun createTempLicenseFile(license: String): File {
    val licenseFile = File.createTempFile("jira-license", ".txt")
    licenseFile
        .bufferedWriter()
        .use { it.write(license) }
    return licenseFile

}

fun Database.withLicenseString(licenses: List<String>) = LicenseOverridingMysql.Builder(this).licenseStrings(licenses).build()