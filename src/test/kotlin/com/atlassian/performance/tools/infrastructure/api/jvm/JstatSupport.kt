package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.assertInterruptedJava
import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.time.Duration
import java.time.Duration.ofSeconds

class JstatSupport(
    private val jdk: VersionedJavaDevelopmentKit
) {
    private val jarName = "hello-world-after-1m-wait.jar"
    private val jarResource = "/com/atlassian/performance/tools/infrastructure/api/jvm/$jarName"
    private val timestampLength = "2018-12-17T14:10:44+00:00 ".length

    fun shouldSupportJstat() {
        DockerInfrastructure().use { infra ->
            val ssh = infra.serveSsh()
            ssh.newConnection().use { connection ->
                shouldSupportJstat(ssh, connection)
            }
        }
    }

    private fun shouldSupportJstat(ssh: Ssh, connection: SshConnection) {
        Ubuntu().install(connection, listOf("screen"), Duration.ofMinutes(2))
        connection.upload(File(javaClass.getResource(jarResource)!!.toURI()), jarName)
        jdk.install(connection)
        val hello = ssh.runInBackground(jdk.command("-classpath $jarName samples.HelloWorld"))
        val pid = IdempotentAction("Wait for the Hello, World! process to start.") {
            connection.execute("cat hello-world.pid").output
        }.retry(
            maxAttempts = 3,
            backoff = ExponentialBackoff(ofSeconds(1))
        )
        val jstatMonitoring = jdk.jstatMonitoring.start(connection, pid.toInt())
        waitForJstatToCollectSomeData()
        jstatMonitoring.stop(connection)
        hello.stop(ofSeconds(1)).assertInterruptedJava();
        val jstatLog = connection.execute("head -n 1 ${jstatMonitoring.getResultPath()}").output
        val jstatHeader = jstatLog.substring(timestampLength, jstatLog.indexOf('\n'))

        assertThat(jstatHeader).contains(
            "Timestamp", "S0", "S1", "E", "O", "M", "CCS", "YGC", "YGCT", "FGC", "FGCT", "GCT"
        )
    }

    private fun waitForJstatToCollectSomeData() {
        Thread.sleep(ofSeconds(8).toMillis())
    }
}
