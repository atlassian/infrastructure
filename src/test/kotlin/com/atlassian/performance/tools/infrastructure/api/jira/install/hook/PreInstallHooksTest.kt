package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PreInstallHook
import com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.mock.UnimplementedSshConnection
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshHost
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Paths

class PreInstallHooksTest {

    private val dummySsh = Ssh(SshHost("localhost", "dummyUser", Paths.get("dummyKey")))
    private val dummyHttp = HttpNode(
        TcpNode("dummyPublicIp", "dummyPrivateIp", 123, "fake-server", dummySsh),
        "/",
        false
    )

    @Test
    fun shouldInsertDuringListing() {
        val counter = CountingHook()
        val hooks = PreInstallHooks.empty().apply {
            insert(counter)
            insert(InsertingHook(counter))
            insert(counter)
        }

        hooks.call(UnimplementedSshConnection(), dummyHttp, Reports())

        assertThat(counter.count).isEqualTo(3)
    }

    @Test
    fun shouldHookToTheTailDuringListing() {
        val counter = CountingHook()
        val hooks = PreInstallHooks.empty().apply {
            insert(counter)
            insert(counter)
            insert(InsertingHook(counter))
        }

        hooks.call(UnimplementedSshConnection(), dummyHttp, Reports())

        assertThat(counter.count).isEqualTo(3)
    }
}

private class CountingHook : PreInstallHook {

    var count = 0

    override fun call(ssh: SshConnection, http: HttpNode, hooks: PreInstallHooks, reports: Reports) {
        count++
    }
}

private class InsertingHook(
    private val hook: PreInstallHook
) : PreInstallHook {
    override fun call(ssh: SshConnection, http: HttpNode, hooks: PreInstallHooks, reports: Reports) {
        hooks.insert(hook)
    }
}
