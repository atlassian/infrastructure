package com.atlassian.performance.tools.infrastructure.hookapi.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.hookapi.jira.start.hook.PreStartHooks
import com.atlassian.performance.tools.infrastructure.jira.install.hook.ProfilerHook
import com.atlassian.performance.tools.infrastructure.jira.install.hook.SplunkForwarderHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class PostInstallHooks private constructor(
    val preStart: PreStartHooks
) {

    private val hooks: Queue<PostInstallHook> = ConcurrentLinkedQueue()
    val postStart = preStart.postStart

    fun insert(
        hook: PostInstallHook
    ) {
        hooks.add(hook)
    }

    internal fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        reports: Reports
    ) {
        while (true) {
            hooks
                .poll()
                ?.call(ssh, jira, this, reports)
                ?: break
        }
    }

    companion object Factory {
        fun default(): PostInstallHooks = PostInstallHooks(PreStartHooks.default()).apply {
            val config = JiraNodeConfig.Builder().build()
            listOf(
                JiraHomeProperty(),
                DisabledAutoBackup(),
                JvmConfig(config),
                ProfilerHook(config.profiler),
                SplunkForwarderHook(config.splunkForwarder),
                JiraLogs(),
                LateUbuntuSysstat()
            ).forEach { insert(it) }
        }

        fun empty(): PostInstallHooks = PostInstallHooks(PreStartHooks.empty())
    }
}
