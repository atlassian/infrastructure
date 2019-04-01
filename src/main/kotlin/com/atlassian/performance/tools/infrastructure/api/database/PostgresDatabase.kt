package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.api.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.time.Duration
import java.time.Instant

class PostgresDatabase(
    private val source: DatasetPackage,
    val dbName: String = "atldb",
    val dbUser: String = "postgres",
    val dbPassword: String ="postgres"
) : Database{

    private val logger: Logger = LogManager.getLogger(this::class.java)

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
        setupSharedBuffer(ssh)
        image.run(
            ssh = ssh,
            parameters = "-p 5432:5432 -v `realpath $postgresBinaryData`:/var/lib/postgresql/data"
        )
        return postgresBinaryData
    }

    override fun start(jira: URI, ssh: SshConnection) {
        waitForPostgres(ssh)
        replaceJiraUrl(ssh, jira)
    }

    private fun setupSharedBuffer(ssh: SshConnection){
        try{
            val result = ssh.execute("grep MemTotal /proc/meminfo | awk '{print $2}'")
            val memory = result.output.trim().toLong() / 4096 //to MB
            ssh.execute("sed -i 's/.*shared_buffers[ ]*\\=.*/shared_buffers = ${memory}MB/' database/postgresql.conf")
            logger.info("shared_buffers has been updated to ${memory}MB")
        } catch(e : Exception){
            logger.error("Fail to update the shared buffer")
            logger.error(e)
        }

    }

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
        ssh.execute("""$connectStr "UPDATE propertystring SET propertyvalue = '$jira' WHERE id IN (select id from propertyentry where property_key like '%baseurl%');" """)
    }
}