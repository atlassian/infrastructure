package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions
import org.junit.Test
import java.lang.Exception

class ThreadDumpIT {
    @Test
    fun shouldGatherThreadDump() {
        val destination = "thread-dumps"
        val jdk = OracleJDK()
        SshUbuntuContainer().start().use { sshUbuntu ->
            sshUbuntu.toSsh().newConnection().use { sshConnection ->
                jdk.install(sshConnection)
                sshConnection.execute("""echo "public class Test { public static void main(String[] args) { try { Thread.sleep(java.time.Duration.ofMinutes(1).toMillis()); } catch (InterruptedException e) { throw new RuntimeException(e); } }}" > Test.java """.trimIndent())
                sshConnection.execute("${jdk.use()}; javac Test.java")
                val process = sshConnection.startProcess("${jdk.use()}; java Test")
                try {
                    val pid = getPid(sshConnection, jdk)

                    ThreadDump(pid, jdk).gather(sshConnection, destination)

                    val threadDumpFile = sshConnection.execute("ls $destination").output
                    val threadDump = sshConnection.execute("cat $destination/$threadDumpFile").output
                    Assertions.assertThat(threadDump).contains("Full thread dump Java HotSpot")
                } catch (e: Exception) {
                    throw Exception(e)
                } finally {
                    sshConnection.stopProcess(process)
                }
            }
        }
    }

    private fun getPid(sshConnection: SshConnection, jdk: OracleJDK): Int {
        val sshResult = sshConnection.execute("${jdk.use()}; jcmd")
        return sshResult
            .output.split("\n")
            .single { it.contains("Test") }
            .split(" ")
            .first()
            .toInt()
    }
}
