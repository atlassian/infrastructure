package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions
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
    private val timestampLength = "2018-12-17T14:10:44+00:00 ".length

    fun shouldSupportJstat(connection: SshConnection) {
        Ubuntu().install(connection, listOf("curl screen"), Duration.ofMinutes(2))
        connection.upload(File(this.javaClass.getResource(jar).toURI()), jarName)
        jdk.install(connection)
        connection.startProcess(jdk.command("-classpath $jarName samples.HelloWorld"))
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
        val jstatLog = connection.execute("cat ${jstatMonitoring.getResultPath()}").output
        val jstatHeader = jstatLog.substring(timestampLength, jstatLog.indexOf('\n'))

        Assertions.assertThat(jstatHeader).contains(this.expectedStats)
    }

    private fun waitForJstatToCollectSomeData() {
        Thread.sleep(4 * 1000)
    }
}
