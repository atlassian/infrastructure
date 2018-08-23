package com.atlassian.performance.tools.infrastructure.api.dataset

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.jvmtasks.TaskTimer.time
import com.atlassian.performance.tools.ssh.SshConnection
import java.time.Duration

class FileArchiver {
    private val ubuntu = Ubuntu()

    fun unzip(
        connection: SshConnection,
        archive: String,
        timeout: Duration
    ) {
        ubuntu.install(connection, listOf("pbzip2"))
        time("unzip") {
            connection.execute("pbzip2 -dc $archive | tar x -C .", timeout)
        }
    }

    fun zip(
        connection: SshConnection,
        toArchive: String,
        timeout: Duration,
        memory: Int = 100,
        level: Int = 9
    ): String {
        ubuntu.install(connection, listOf("pbzip2"))
        val destination = zippedName(toArchive)
        time("zip") {
            // in some cases we need sudo, because of file permissions e.g. MySQL database
            connection.execute("sudo tar -c $toArchive | pbzip2 -c -$level -m$memory > $destination", timeout)
        }
        return destination
    }

    fun zippedName(name: String) = "$name.tar.bz2"

    fun pipeUnzip(
        connection: SshConnection
    ): String {
        ubuntu.install(connection, listOf("pbzip2"))

        return "pbzip2 -dc | tar x -C ."
    }
}