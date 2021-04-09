package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.time.Duration

class JstatSupport(
    private val jdk: VersionedJavaDevelopmentKit
) {
    private val expectedStats: Set<String> = setOf(
        "Timestamp",
        "S0",
        "S1",
        "E",
        "O",
        "M",
        "CCS",
        "YGC",
        "YGCT",
        "FGC",
        "FGCT",
        "GCT"
    )
    private val jarName = "hello-world-after-1m-wait.jar"
    private val jar = "/com/atlassian/performance/tools/infrastructure/api/jvm/$jarName"

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
        connection.upload(File(this.javaClass.getResource(jar).toURI()), jarName)
        jdk.install(connection)
        ssh.runInBackground(jdk.command("-classpath $jarName samples.HelloWorld"))
        val pid = IdempotentAction("Wait for the Hello, World! process to start.") {
            connection.execute("cat hello-world.pid").output
        }.retry(
            maxAttempts = 3,
            backoff = ExponentialBackoff(Duration.ofSeconds(1))
        )
        val jstatMonitoring = jdk.jstatMonitoring.start(connection, pid.toInt())
        waitForJstatToCollectSomeData()
        jstatMonitoring.stop(connection)
        val jstatLog = connection.execute("head -n 1 ${jstatMonitoring.getResultPath()}").output

        assertThat(jstatLog).contains(this.expectedStats)
    }

    private fun waitForJstatToCollectSomeData() {
        Thread.sleep(8 * 1000)
    }
}
