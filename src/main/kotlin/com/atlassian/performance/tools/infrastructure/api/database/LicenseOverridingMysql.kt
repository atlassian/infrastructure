package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.database.SshMysqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

/**
 * Removes all licenses from [database] and adds [licenses] instead.
 *
 * @param [database] must be MySQL
 * @since 4.13.0
 */
class LicenseOverridingMysql(
    private val database: Database,
    private val licenses: List<String>
) : Database {

    override fun setup(
        ssh: SshConnection
    ): String = database.setup(ssh)

    override fun start(
        jira: URI,
        ssh: SshConnection
    ) {
        database.start(jira, ssh)
        val firstLicense = licenses.first()
        val licenseTable = "jiradb.productlicense"
        val sql = "DELETE FROM $licenseTable; REPLACE INTO $licenseTable (LICENSE) VALUES (\"$firstLicense\");"
        val mysql = SshMysqlClient()
        mysql.runSql(ssh, sql)
        licenses.drop(1).forEach { license ->
            mysql.runSql(
                ssh = ssh,
                sql = "INSERT INTO $licenseTable SELECT MAX(id)+1, \"$license\" FROM $licenseTable;"
            )
        }
    }
}