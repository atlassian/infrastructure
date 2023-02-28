package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test
import java.net.URI
import java.util.function.Consumer

class MinimalMysqlDatabaseIT {
    @Test
    fun `can be used by jiradbuser`() {
        val assumedUser = "jiradbuser"
        val password = "static-integration-test-password"
        val mysql = MinimalMysqlDatabase.Builder()
            .jiraDbUserPassword(password)
            .build()

        SshUbuntuContainer.Builder()
            .customization(Consumer { it.setPrivilegedMode(true) })
            .build()
            .start()
            .use { ubuntu ->
                ubuntu.toSsh().newConnection().use { ssh ->
                    mysql.setup(ssh)
                    mysql.start(URI("https://dummy-jira.net"), ssh)

                    ssh.execute("mysql -h 127.0.0.1 -u $assumedUser -p$password -e 'USE jiradb; SHOW TABLES;'")
                }
            }
    }
}
