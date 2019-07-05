package com.atlassian.performance.tools.infrastructure.mock

import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection.SshResult
import org.apache.logging.log4j.Level
import java.io.File
import java.nio.file.Path
import java.time.Duration

class UnimplementedSshConnection : SshConnection {

    override fun close() = fail<Unit>()
    override fun download(remoteSource: String, localDestination: Path) = fail<Unit>()
    override fun execute(cmd: String, timeout: Duration, stdout: Level, stderr: Level): SshResult = fail()
    override fun safeExecute(cmd: String, timeout: Duration, stdout: Level, stderr: Level): SshResult = fail()
    override fun startProcess(cmd: String): DetachedProcess = fail()
    override fun stopProcess(process: DetachedProcess) = fail<Unit>()
    override fun upload(localSource: File, remoteDestination: String) = fail<Unit>()

    private fun <T> fail(): T = throw Exception("Unexpected call")
}