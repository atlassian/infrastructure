package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntu
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.After
import org.junit.Before
import org.junit.Test
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