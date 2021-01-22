package com.atlassian.performance.tools.infrastructure.api.jira.hook

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PreInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.TcpServer
import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.nio.file.Path
import java.time.Duration

class JiraNodeHooksTest {

    @Test
    fun shouldHookDuringListing() {
        val counter = CountingHook()
        val hooks = JiraNodeHooks.empty().apply {
            preInstall.insert(counter)
            preInstall.insert(HookingHook(counter))
            preInstall.insert(counter)
        }
        val server = TcpServer("doesn't matter", 123, "fake-server")

        hooks.preInstall.call(FailingSshConnection(), server)

        assertThat(counter.count).isEqualTo(3)
    }

    @Test
    fun shouldHookToTheTailDuringListing() {
        val counter = CountingHook()
        val hooks = JiraNodeHooks.empty().apply {
            preInstall.insert(counter)
            preInstall.insert(counter)
            preInstall.insert(HookingHook(counter))
        }
        val server = TcpServer("doesn't matter", 123, "fake-server")

        hooks.preInstall.call(FailingSshConnection(), server)

        assertThat(counter.count).isEqualTo(3)
    }
}

private class CountingHook : PreInstallHook {

    var count = 0

    override fun call(ssh: SshConnection, server: TcpServer, hooks: PreInstallHooks) {
        count++
    }
}

private class HookingHook(
    private val hook: PreInstallHook
) : PreInstallHook {
    override fun call(ssh: SshConnection, server: TcpServer, hooks: PreInstallHooks) {
        hooks.insert(hook)
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
