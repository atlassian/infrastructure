package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshHost
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntu
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
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
    private lateinit var sshUbuntu: SshUbuntu

    @Before
    fun before() {
        executor = Executors.newCachedThreadPool()
        sshUbuntu = SshUbuntuContainer().start()
    }

    @After
    fun after() {
        sshUbuntu.close()
        executor.shutdownNow()
    }

    @Test
    fun shouldRetry() {
        sshUbuntu.toSsh().newConnection().use { connection ->
            Ubuntu().install(
                ColdAptSshConnection(connection),
                listOf("vim"),
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

        (1..concurrency)
            .map {
                executor.submit { installLftp(lock, latch) }
            }.map { it.get(5, TimeUnit.MINUTES) }

    }

    private fun installLftp(lock: Any, latch: CountDownLatch) {
        val ssh = synchronized(lock) {
            sshUbuntu.toSsh()
        }
        ssh.newConnection().use { connection ->
            latch.countDown()
            latch.await()
            Ubuntu().install(connection, listOf("lftp"))
        }
    }

}