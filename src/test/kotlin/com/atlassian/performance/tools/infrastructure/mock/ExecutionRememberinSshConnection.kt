package com.atlassian.performance.tools.infrastructure.mock

import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.Level
import java.io.File
import java.time.Duration

class ExecutionRememberingSshConnection : SshConnection by UnimplementedSshConnection() {

    val commands = mutableListOf<String>()
    val uploads = mutableListOf<Upload>()

    override fun upload(localSource: File,
                        remoteDestination: String)
    {
        uploads.add(Upload(localSource, remoteDestination, localSource.readText() ))
    }

    override fun execute(
        cmd: String,
        timeout: Duration,
        stdout: Level,
        stderr: Level
    ): SshConnection.SshResult {
        commands.add(cmd)
        return SshConnection.SshResult(
            exitStatus = 0,
            output = "",
            errorOutput = ""
        )
    }

    class Upload(val localSource: File, val remoteDestination: String, val content: String)
}
