package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.threeten.extra.Interval
import java.io.File
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

class UbuntuTest {

    @Test
    fun shouldBeParallelOnDifferentSshConnections() {
        val executor = Executors.newCachedThreadPool()
        val concurrency = 4

        val singleInstall = Supplier {
            TimingSshConnection().use { ssh ->
                Ubuntu().install(ssh, listOf("whatever"))
                ssh.timeInteraction()
            }
        }
        val installs = (1..concurrency).map {
            CompletableFuture.supplyAsync(singleInstall, executor)
        }
        val sshInteractions = installs.map { it.get(5, TimeUnit.SECONDS) }

        val actualOverlaps = sshInteractions.zipWithNext { a, b -> a.overlaps(b) }
        val expectedOverlaps = (1 until concurrency).map { true }
        assertThat(actualOverlaps)
            .`as`("all SSH interactions overlap: $sshInteractions")
            .isEqualTo(expectedOverlaps)
    }

    private class TimingSshConnection : SshConnection {
        private var firstRequest: Instant? = null
        private var lastResponse: Instant? = null

        fun timeInteraction(): Interval = Interval.of(
            firstRequest ?: throw Exception("There was no first SSH interaction"),
            lastResponse ?: throw Exception("There was no last SSH interaction")
        )

        override fun execute(
            cmd: String,
            timeout: Duration,
            stdout: Level,
            stderr: Level
        ): SshConnection.SshResult {
            if (firstRequest == null) {
                firstRequest = Instant.now()
            }
            Thread.sleep(300)
            lastResponse = Instant.now()
            return SshConnection.SshResult(0, "", "")
        }

        override fun safeExecute(
            cmd: String,
            timeout: Duration,
            stdout: Level,
            stderr: Level
        ): SshConnection.SshResult {
            return execute(cmd, timeout, stdout, stderr)
        }

        override fun close() {
        }

        override fun download(remoteSource: String, localDestination: Path) {
            throw Exception("Unexpected call")
        }

        override fun startProcess(cmd: String): DetachedProcess {
            throw Exception("Unexpected call")
        }

        override fun stopProcess(process: DetachedProcess) {
            throw Exception("Unexpected call")
        }

        override fun upload(localSource: File, remoteDestination: String) {
            throw Exception("Unexpected call")
        }
    }
}
