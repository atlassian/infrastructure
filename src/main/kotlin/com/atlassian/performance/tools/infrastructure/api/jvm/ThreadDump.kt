package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Instant

/**
 * Creates Thread Dumps on a remote server over an ssh connection.
 *
 * @param pid process identifier.
 * @param jdk java development kit.
 *
 * @since 4.9.0
 */
class ThreadDump(
    private val pid: Int,
    private val jdk: JavaDevelopmentKit
) {
    /**
     * Creates a Thread Dump from a process with [pid]
     * in [destination] folder.
     *
     * @param connection ssh connection to a remote machine.
     * @param destination destination folder for thread dumps.
     *
     * @since 4.9.0
     */
    fun gather(connection: SshConnection, destination: String) {
        val threadDumpName = Instant.now().toEpochMilli()
        val command = "${jdk.use()}; jcmd $pid Thread.print > $destination/${threadDumpName}"
        connection.execute("mkdir -p $destination")
        connection.execute(command)
    }
}
