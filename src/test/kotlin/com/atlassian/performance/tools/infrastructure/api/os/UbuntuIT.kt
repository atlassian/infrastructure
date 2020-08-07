package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuContainer
import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage.Companion.runSoloSsh
import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage.Companion.runSoloUbuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshHost
import org.apache.logging.log4j.Level
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UbuntuIT {
    private lateinit var executor: ExecutorService

    @Before
    fun before() {
        executor = Executors.newCachedThreadPool()
    }

    @After
    fun after() {
        executor.shutdownNow()
    }

    @Test
    fun shouldRetry() {
        runSoloSsh { ssh ->
            Ubuntu().install(
                ColdAptSshConnection(ssh),
                listOf("nano"),
                Duration.ofSeconds(30)
            )
        }
    }

    private class ColdAptSshConnection(
        val connection: SshConnection
    ) : SshConnection by connection {

        private var cold = true

        override fun execute(
            cmd: String,
            timeout: Duration,
            stdout: Level,
            stderr: Level
        ): SshConnection.SshResult {
            val overriddenCommand = if (cmd.contains("apt-get install")) {
                throttleApt(cmd)
            } else {
                cmd
            }
            return connection.execute(overriddenCommand, timeout, stdout, stderr)
        }

        private fun throttleApt(
            cmd: String
        ): String = if (cold) {
            cold = false
            cmd.replace(
                oldValue = "apt-get install",
                newValue = "apt-get -o Acquire::http::Dl-Limit=1 install"
            )
        } else {
            cmd
        }

        override fun getHost(): SshHost {
            return connection.getHost()
        }
    }

    @Test
    fun shouldBeThreadSafe() {
        val lock = Object()
        val concurrency = 5
        val latch = CountDownLatch(concurrency)

        runSoloUbuntu { ubuntu ->
            (1..concurrency)
                .map {
                    executor.submit { installLftp(lock, latch, ubuntu) }
                }.map { it.get(5, TimeUnit.MINUTES) }
        }
    }

    private fun installLftp(lock: Any, latch: CountDownLatch, ubuntu: SshUbuntuContainer) {
        val ssh = synchronized(lock) {
            ubuntu.ssh
        }
        ssh.newConnection().use { connection ->
            latch.countDown()
            latch.await()
            Ubuntu().install(connection, listOf("lftp"))
        }
    }
}
