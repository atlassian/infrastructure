package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.TcpServerHook
import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.nio.file.Path
import java.time.Duration

class JiraNodeFlowTest {

    @Test
    fun shouldHookDuringListing() {
        val counter = CountingHook()
        val flow = JiraNodeFlow().apply {
            hookPreInstall(counter)
            hookPreInstall(HookingHook(counter))
            hookPreInstall(counter)
        }
        val server = TcpServer("doesn't matter", 123, "fake-server")

        flow.runPreInstallHooks(FailingSshConnection(), server)

        assertThat(counter.count).isEqualTo(3)
    }

    @Test
    fun shouldHookToTheTailDuringListing() {
        val counter = CountingHook()
        val flow = JiraNodeFlow().apply {
            hookPreInstall(counter)
            hookPreInstall(counter)
            hookPreInstall(HookingHook(counter))
        }
        val server = TcpServer("doesn't matter", 123, "fake-server")

        flow.runPreInstallHooks(FailingSshConnection(), server)

        assertThat(counter.count).isEqualTo(3)
    }
}

private class CountingHook : TcpServerHook {

    var count = 0

    override fun run(ssh: SshConnection, server: TcpServer, flow: JiraNodeFlow) {
        count++
    }
}

private class HookingHook(
    private val hook: TcpServerHook
) : TcpServerHook {
    override fun run(ssh: SshConnection, server: TcpServer, flow: JiraNodeFlow) {
        flow.hookPreInstall(hook)
    }
}

private class FailingSshConnection : SshConnection {
    override fun close() {
        throw Exception("unexpected call")
    }

    override fun download(remoteSource: String, localDestination: Path) {
        throw Exception("unexpected call")
    }

    override fun execute(cmd: String, timeout: Duration, stdout: Level, stderr: Level): SshConnection.SshResult {
        throw Exception("unexpected call")
    }

    override fun safeExecute(cmd: String, timeout: Duration, stdout: Level, stderr: Level): SshConnection.SshResult {
        throw Exception("unexpected call")
    }

    override fun startProcess(cmd: String): DetachedProcess {
        throw Exception("unexpected call")
    }

    override fun stopProcess(process: DetachedProcess) {
        throw Exception("unexpected call")
    }

    override fun upload(localSource: File, remoteDestination: String) {
        throw Exception("unexpected call")
    }
}
