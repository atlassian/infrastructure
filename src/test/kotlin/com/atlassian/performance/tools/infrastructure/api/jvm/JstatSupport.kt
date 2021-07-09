package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.assertInterruptedJava
import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.time.Duration

class JstatSupport(
    private val jdk: VersionedJavaDevelopmentKit,
    private val ssh: Ssh
) {
    private val jarName = "hello-world-after-1m-wait.jar"
    private val jarResource = "/com/atlassian/performance/tools/infrastructure/api/jvm/$jarName"
    private val timestampLength = "2018-12-17T14:10:44+00:00 ".length

    fun shouldSupportJstat() {
        ssh.newConnection().use { connection ->
            shouldSupportJstat(connection)
        }
    }

    private fun shouldSupportJstat(connection: SshConnection) {
        Ubuntu().install(connection, listOf("curl", "screen"), Duration.ofMinutes(2))
        connection.upload(File(javaClass.getResource(jarResource).toURI()), jarName)
        jdk.install(connection)
        val hello = ssh.runInBackground(jdk.command("-classpath $jarName samples.HelloWorld"))
        val pid = IdempotentAction(
            description = "Wait for the Hello, World! process to start.",
            action = { connection.execute("cat hello-world.pid").output }
        ).retry(
            maxAttempts = 3,
            backoff = ExponentialBackoff(baseBackoff = Duration.ofSeconds(1))
        )
        val jstatMonitoring = jdk.jstatMonitoring.start(connection, pid.toInt())
        waitForJstatToCollectSomeData()
        jstatMonitoring.stop(connection)
        hello.stop(Duration.ofSeconds(1)).assertInterruptedJava();
        val jstatLog = connection.execute("cat ${jstatMonitoring.getResultPath()}").output
        val jstatHeader = jstatLog.substring(timestampLength, jstatLog.indexOf('\n'))

        assertThat(jstatHeader).contains(
            "Timestamp", "S0", "S1", "E", "O", "M", "CCS", "YGC", "YGCT", "FGC", "FGCT", "GCT"
        )
    }

    private fun waitForJstatToCollectSomeData() {
        Thread.sleep(Duration.ofSeconds(4).toMillis())
    }
}
