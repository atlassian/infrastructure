package com.atlassian.performance.tools.infrastructure.database

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.dataset.DatasetPackage
import com.atlassian.performance.tools.infrastructure.os.Ubuntu
import com.atlassian.performance.tools.ssh.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.time.Duration
import java.time.Instant

class MySqlDatabase(
    private val source: DatasetPackage
) : Database {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    private val image: DockerImage = DockerImage("mysql:5.6.38")
    private val ubuntu = Ubuntu()

    override fun setup(ssh: SshConnection): String {
        val mysqlData = source.download(ssh)
        image.run(
            ssh = ssh,
            parameters = "-p 3306:3306 -v `realpath $mysqlData`:/var/lib/mysql",
            arguments = "--skip-grant-tables"
        )
        return mysqlData
    }

    override fun start(jira: URI, ssh: SshConnection) {
        waitForMysql(ssh)
        ssh.execute("""mysql -h 127.0.0.1  -u root -e "UPDATE jiradb.propertystring SET propertyvalue = '$jira' WHERE id IN (select id from jiradb.propertyentry where property_key like '%baseurl%');" """)
    }

    private fun waitForMysql(ssh: SshConnection) {
        ubuntu.install(ssh, listOf("mysql-client"))
        val mysqlStart = Instant.now()
        while (!ssh.safeExecute("mysql -h 127.0.0.1 -u root -e 'select 1;'").isSuccessful()) {
            if (Instant.now() > mysqlStart + Duration.ofMinutes(15)) {
                throw RuntimeException("MySql didn't start in time")
            }
            logger.debug("Waiting for MySQL...")
            Thread.sleep(Duration.ofSeconds(10).toMillis())
        }
    }
}