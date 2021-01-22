package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.jira.hook.install.ProfilerHook
import com.atlassian.performance.tools.infrastructure.jira.hook.install.SplunkForwarderHook
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultPostInstallHook(
    private val config: JiraNodeConfig
) : PostInstallHook {

    override fun call(
        ssh: SshConnection,
        jira: InstalledJira,
        hooks: PostInstallHooks
    ) {
        listOf(
            JiraHomeProperty(),
            DisabledAutoBackup(),
            JvmConfig(config),
            ProfilerHook(config.profiler),
            SplunkForwarderHook(config.splunkForwarder),
            JiraLogs(),
            LateUbuntuSysstat()
        ).forEach { it.call(ssh, jira, hooks) }
    }
}
