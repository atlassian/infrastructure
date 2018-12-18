package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.UbuntuContainer
import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import org.hamcrest.Matchers.startsWith
import org.junit.Assert
import java.io.File
import java.time.Duration

class JstatSupport(
    private val jdk: JavaDevelopmentKit
) {
    private val jarName = "hello-world-after-1m-wait.jar"
    private val jar = "/com/atlassian/performance/tools/infrastructure/api/jvm/$jarName"
    private val expectedJstatHeader = "Timestamp         S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT     GCT"
    private val timestampLength = "2018-12-17T14:10:44+00:00 ".length

    fun shouldSupportJstat() {
        UbuntuContainer().run { ssh ->
            ssh.execute("apt-get install curl screen -y -qq", Duration.ofMinutes(2))
            ssh.upload(File(this.javaClass.getResource(jar).toURI()), jarName)
            jdk.install(ssh)
            ssh.startProcess(jdk.command("-classpath $jarName samples.HelloWorld"))
            val pid = IdempotentAction(
                description = "Wait for the Hello, World! process to start.",
                action = { ssh.execute("cat hello-world.pid").output }
            ).retry(
                maxAttempts = 3,
                backoff = ExponentialBackoff(baseBackoff = Duration.ofSeconds(1))
            )
            val jstatMonitoring = jdk.jstatMonitoring.startMonitoring(ssh, pid)
            waitForJstatToCollectSomeData()
            ssh.stopProcess(jstatMonitoring.process)
            val jstatLog = ssh.execute("cat ${jstatMonitoring.logFile}").output
            val jstatHeader = jstatLog.substring(timestampLength, jstatLog.indexOf('\n'))
            Assert.assertThat(jstatHeader, startsWith(this.expectedJstatHeader))
        }
    }

    private fun waitForJstatToCollectSomeData() {
        Thread.sleep(4*1000)
    }
}