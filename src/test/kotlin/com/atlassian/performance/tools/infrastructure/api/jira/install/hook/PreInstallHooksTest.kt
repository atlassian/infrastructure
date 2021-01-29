package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpServer
import com.atlassian.performance.tools.infrastructure.mock.UnimplementedSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JiraNodeHooksTest {

    @Test
    fun shouldHookDuringListing() {
        val counter = CountingHook()
        val hooks = PreInstallHooks.empty().apply {
            insert(counter)
            insert(HookingHook(counter))
            insert(counter)
        }
        val server = TcpServer("doesn't matter", 123, "fake-server")

        hooks.call(UnimplementedSshConnection(), server)

        assertThat(counter.count).isEqualTo(3)
    }

    @Test
    fun shouldHookToTheTailDuringListing() {
        val counter = CountingHook()
        val hooks = PreInstallHooks.empty().apply {
            insert(counter)
            insert(counter)
            insert(HookingHook(counter))
        }
        val server = TcpServer("doesn't matter", 123, "fake-server")

        hooks.call(UnimplementedSshConnection(), server)

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
