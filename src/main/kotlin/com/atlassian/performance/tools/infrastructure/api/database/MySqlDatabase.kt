package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.database.Mysql
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

/**
 * Since 4.21.0, it's compatible with Ubuntu 16.04, 18.04, 20.04 and 22.04.
 * Note: We decided to use `Mysql` casing in this repository. Please don't repeat `MySql`.
 *
 * @param maxConnections MySQL `max_connections` parameter.
 */
class MySqlDatabase(
    private val source: DatasetPackage,
    private val maxConnections: Int
) : Database {

    /**
     * Uses MySQL defaults.
     */
    constructor(
        source: DatasetPackage
    ) : this(
        source = source,
        maxConnections = 151
    )

    override fun setup(ssh: SshConnection): String {
        val mysqlDataLocation = source.download(ssh)
        Mysql
            .container(
                dataDir = mysqlDataLocation,
                extraParameters = emptyArray(),
                extraArguments = arrayOf(
                    "--skip-grant-tables", // Recovery mode, as some datasets give no permissions to their root DB user
                    "--max_connections=$maxConnections"
                )
            )
            .run(ssh)
        return mysqlDataLocation
    }

    override fun start(jira: URI, ssh: SshConnection) {
        val client = Mysql.installClient(ssh)
        Mysql.awaitDatabase(ssh, client)

        client.runSql(ssh, "UPDATE jiradb.propertystring SET propertyvalue = '$jira' WHERE id IN (select id from jiradb.propertyentry where property_key like '%baseurl%');")
    }
}
