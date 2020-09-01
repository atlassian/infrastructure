package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.concurrency.api.AbruptExecutorService
import com.atlassian.performance.tools.concurrency.api.submitWithLogContext
import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage.Companion.runSoloSsh
import org.junit.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.TimeUnit

class DockerIT {

    @Test
    fun shouldRunOnVariousUbuntuVersions() {
        AbruptExecutorService(newCachedThreadPool()).use { pool ->
            listOf("16.04", "18.04")
                .map { ubuntuVersion ->
                    pool.submitLabelled("test $ubuntuVersion") {
                        testDockerRun(ubuntuVersion)
                    }
                }
                .forEach { it.get(5, TimeUnit.MINUTES) }
        }
    }

    private fun testDockerRun(ubuntuVersion: String): String {
        return runSoloSsh(ubuntuVersion) { ssh ->
            Docker().install(ssh)
            DockerImage("hello-world").run(ssh)
        }
    }
}

private fun <T> ExecutorService.submitLabelled(
    label: String,
    task: () -> T
): CompletableFuture<T> {
    return submitWithLogContext(label) {
        try {
            task()
        } catch (e: Exception) {
            throw Exception("Failed to $label", e)
        }
    }
}
