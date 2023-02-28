package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.database.Mysql
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

/**
 * Compatible with Ubuntu 16.04, 18.04, 20.04 and 22.04.
 * @param maxConnections MySQL `max_connections` parameter.
 */
class MysqlDatabase private constructor(
    private val source: DatasetPackage,
    private val maxConnections: Int
) : Database {
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
        Mysql.awaitDatabase(ssh)

        client.runSql(ssh, "UPDATE jiradb.propertystring SET propertyvalue = '$jira' WHERE id IN (select id from jiradb.propertyentry where property_key like '%baseurl%');")
    }

    class Builder(
        private var source: DatasetPackage
    ) {
        private var maxConnections: Int = 151

        fun source(source: DatasetPackage) = apply { this.source = source }

        fun maxConnections(maxConnections: Int) = apply { this.maxConnections = maxConnections }

        fun build() = MysqlDatabase(
            source,
            maxConnections
        )
    }
}

/**
 * Since 4.21.0, it's compatible with Ubuntu 16.04, 18.04, 20.04 and 22.04.
 * @param maxConnections MySQL `max_connections` parameter.
 */
@Deprecated("Use `MysqlDatabase` for naming consistency")
class MySqlDatabase(
    private val source: DatasetPackage,
    private val maxConnections: Int
) : Database by MysqlDatabase.Builder(source)
    .maxConnections(maxConnections)
    .build()
{
    /**
     * Uses MySQL defaults.
     */
    constructor(
        source: DatasetPackage
    ) : this(
        source = source,
        maxConnections = 151
    )
}