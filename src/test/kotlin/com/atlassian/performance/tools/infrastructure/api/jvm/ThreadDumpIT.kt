package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import java.time.Duration

class ThreadDumpTest(
    private val jdk: JavaDevelopmentKit
) {
    fun shouldGatherThreadDump() {
        SshUbuntuContainer.Builder().build().start().use { ubuntu ->
            val ssh = ubuntu.toSsh()
            ssh.newConnection().use { connection ->
                shouldGatherThreadDump(ssh, connection)
            }
        }
    }

    private fun shouldGatherThreadDump(ssh: Ssh, connection: SshConnection) {
        jdk.install(connection)
        val destination = "thread-dumps"
        connection.execute("""echo "public class Test { public static void main(String[] args) { try { Thread.sleep(java.time.Duration.ofMinutes(1).toMillis()); } catch (InterruptedException e) { throw new RuntimeException(e); } }}" > Test.java """.trimIndent())
        connection.execute("${jdk.use()}; javac Test.java")
        ssh.runInBackground("${jdk.use()}; java Test").use {
            val pid = IdempotentAction("Get PID") {
                getPid(connection, jdk)
            }.retry(maxAttempts = 2, backoff = StaticBackoff(Duration.ofSeconds(1)))

            ThreadDump(pid, jdk).gather(connection, destination)

            val threadDumpFile = connection.execute("ls $destination").output
            val threadDump = connection.execute("cat $destination/$threadDumpFile").output
            assertThat(threadDump).contains("Full thread dump Java HotSpot")
        }
    }

    private fun getPid(sshConnection: SshConnection, jdk: JavaDevelopmentKit): Int {
        val sshResult = sshConnection.execute("${jdk.use()}; jcmd")
        return sshResult
            .output.split("\n")
            .single { it.contains("Test") }
            .split(" ")
            .first()
            .toInt()
    }
}
