package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration
import java.time.Instant

class PostgresDatabase(
    private val source: DatasetPackage,
    private val dbName: String = "atldb",
    private val dbUser: String = "postgres",
    private val dbPassword: String ="postgres"
) : Database{

    override fun getDbType(): DbType {
        return DbType.Postgres
    }

    private val ubuntu = Ubuntu()
    private val image: DockerImage = DockerImage(
        name = "postgres:10",
        pullTimeout = Duration.ofMinutes(5)
    )

    override fun setup(ssh: SshConnection): String {
        val postgresBinaryData = source.download(ssh)
        image.run(
            ssh = ssh,
            parameters = "-p 5432:5432 -v `realpath $postgresBinaryData`:/var/lib/postgresql/10/main"
        )
        return postgresBinaryData
    }

    override fun start(jira: URI, ssh: SshConnection) {
        waitForPostgres(ssh)
        replaceJiraUrl(ssh, jira)
    }

    //PGPASSWORD=postgres psql -h jkim2-jpte.ci0kcpuzeoud.ap-southeast-2.rds.amazonaws.com -U postgres -d atldb  -c 'select 1;'
    val connectStr = "PGPASSWORD=$dbPassword psql -h 127.0.0.1 -U $dbUser -d $dbName -c"

    private fun waitForPostgres(ssh: SshConnection) {
        ubuntu.install(ssh, listOf("postgresql-client"))
        val start = Instant.now()
        while (!ssh.safeExecute("$connectStr 'select 1;'").isSuccessful()) {
            if (Instant.now() > start + Duration.ofMinutes(15)) {
                throw RuntimeException("Postgres didn't start in time")
            }
            Thread.sleep(Duration.ofSeconds(10).toMillis())
        }
    }

    private fun replaceJiraUrl(ssh: SshConnection, jira: URI) {
        //inject Jira URL via postgres client
        ssh.execute("""$connectStr "UPDATE jiradb.propertystring SET propertyvalue = '$jira' WHERE id IN (select id from jiradb.propertyentry where property_key like '%baseurl%');" """)
    }
}