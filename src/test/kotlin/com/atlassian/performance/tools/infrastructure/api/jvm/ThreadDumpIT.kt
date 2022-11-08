package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions
import java.time.Duration

class ThreadDumpTest {
    fun shouldGatherThreadDump(jdk: JavaDevelopmentKit, connection: SshConnection) {
        jdk.install(connection)
        val destination = "thread-dumps"
        connection.execute("""echo "public class Test { public static void main(String[] args) { try { Thread.sleep(java.time.Duration.ofMinutes(1).toMillis()); } catch (InterruptedException e) { throw new RuntimeException(e); } }}" > Test.java """.trimIndent())
        connection.execute("${jdk.use()}; javac Test.java")
        val process = connection.startProcess("${jdk.use()}; java Test")
        try {
            val pid = IdempotentAction("Get PID") {
                getPid(connection, jdk)
            }.retry(maxAttempts = 2, backoff = StaticBackoff(Duration.ofSeconds(1)))

            ThreadDump(pid, jdk).gather(connection, destination)

            val threadDumpFile = connection.execute("ls $destination").output
            val threadDump = connection.execute("cat $destination/$threadDumpFile").output
            Assertions.assertThat(threadDump).contains("Full thread dump Java HotSpot")
        } catch (e: Exception) {
            throw Exception(e)
        } finally {
            connection.stopProcess(process)
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
