package com.atlassian.performance.tools.infrastructure.jvm

import com.atlassian.performance.tools.ssh.Ssh
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class PeriodicThreadDump(
    private val ssh: Ssh
) {
    private val logger: Logger = LogManager.getLogger(this::class.java)
    private val outputFolder = "thread-dumps" // All thread dumps for a server will land in the same folder, so
    // we shouldn't grab them from the multiple processes.
    private val jdk: JavaDevelopmentKit = OracleJDK()

    private val executor = Executors.newSingleThreadScheduledExecutor(
        ThreadFactoryBuilder()
            .setNameFormat("thread-dumper-%d")
            .setDaemon(true)
            .setUncaughtExceptionHandler { _, exception ->
                logger.error("Exception occurred while gathering thread dumps", exception)
            }
            .build()
    )

    fun start(processId: Int) {
        ssh.newConnection().use {
            it.execute("mkdir -p $outputFolder")
        }

        executor.scheduleWithFixedDelay(
            {
                ssh.newConnection().use { connection ->
                    connection.execute(
                        cmd = """
                        | ${jdk.use()}
                        | jstack $processId > $outputFolder/`date +%s`
                        """.trimMargin(),
                        stdout = Level.TRACE
                    )
                }
            },
            0,
            Duration.ofSeconds(10).seconds,
            TimeUnit.SECONDS)
        logger.debug("Started to gather thread dumps for process $processId")
    }

    fun stopAndGatherResults(resultsFolder: String) {
        executor.shutdown()
        ssh.newConnection().use { connection ->
            connection.execute("tar -czf $outputFolder.tar.gz $outputFolder")
            connection.execute("cp $outputFolder.tar.gz $resultsFolder")
        }
    }
}