package com.atlassian.performance.tools.infrastructure.database


import com.atlassian.performance.tools.infrastructure.mock.RememberingSshConnection
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Files


class SshMysqlClientTest {

    @Test
    fun shouldRunMySqlCommand() {
        val client = SshMysqlClient()
        val ssh = RememberingSshConnection()
        val command = "select * from table"

        client.runSql(ssh, command)

        assertThat(ssh.commands)
                .`as`("SSH commands")
                .containsExactly(
                        "mysql -h 127.0.0.1 -u root -e \"$command\""
                )
    }

    @Test
    fun shouldRunMySqlCommandFromFile() {
        val client = SshMysqlClient()
        val ssh = RememberingSshConnection()
        val file = Files.createTempFile("SshMysqlClientTest", ".txt").toFile()
        file.writeText("anything we want, as we aren't checking this")

        client.runSql(ssh, file)

        assertThat(ssh.commands)
                .`as`("SSH commands")
                .containsExactly(
                        "mysql -h 127.0.0.1 -u root < ${file.name}",
                        "rm ${file.name}"
                )
    }
}
