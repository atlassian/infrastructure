package com.atlassian.performance.tools.infrastructure.api.dataset

import com.atlassian.performance.tools.infrastructure.Ls
import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage.Companion.runSoloUbuntu
import com.atlassian.performance.tools.ssh.api.Ssh
import org.assertj.core.api.Assertions
import org.junit.Test
import java.net.URI
import java.time.Duration
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class HttpDatasetPackageIT {

    @Test
    fun shouldDownloadDataset() {
        val dataset = HttpDatasetPackage(
            uri = URI("https://jpt-custom-datasets-storage-a008820-datasetbucket-1sjxdtrv5hdhj.s3-eu-west-1.amazonaws.com/mock-database.tar.bz2"),
            downloadTimeout = Duration.ofMinutes(1)
        )

        val filesInDataset = runSoloUbuntu { sshUbuntu ->
            val ssh = sshUbuntu.ssh
            return@runSoloUbuntu RandomFilesGenerator(ssh).start().use {
                ssh.newConnection().use { connection ->
                    val unpackedPath = dataset.download(connection)
                    Ls().execute(connection, unpackedPath)
                }
            }
        }

        Assertions
            .assertThat(filesInDataset)
            .containsExactlyInAnyOrder(
                "auto.cnf",
                "ib_logfile0",
                "ib_logfile1",
                "ibdata1",
                "jiradb",
                "mysql",
                "performance_schema"
            )
    }

    private class RandomFilesGenerator(private val ssh: Ssh) {
        fun start(): AutoCloseable {
            val generator = object : AutoCloseable {
                private val executor = Executors.newSingleThreadExecutor()
                private val createNewFiles = AtomicBoolean(true)

                fun start() {
                    executor.execute {
                        while (createNewFiles.get()) {
                            ssh.newConnection().use {
                                it.safeExecute(
                                    cmd = "touch ${UUID.randomUUID()}"
                                )
                            }
                            Thread.sleep(20)
                        }
                    }
                }

                override fun close() {
                    createNewFiles.set(false)
                    executor.shutdown()
                }
            }
            generator.start()
            return generator
        }
    }
}
