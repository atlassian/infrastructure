package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.flow.PostInstallFlow
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.LateUbuntuSysstat
import com.atlassian.performance.tools.infrastructure.jira.flow.install.ProfilerHook
import com.atlassian.performance.tools.infrastructure.jira.flow.install.SplunkForwarderHook
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultPostInstallHook(
    private val config: JiraNodeConfig
) : PostInstallHook {

    override fun run(
        ssh: SshConnection,
        jira: InstalledJira,
        flow: PostInstallFlow
    ) {
        listOf(
            JiraHomeProperty(),
            DisabledAutoBackup(),
            JvmConfig(config),
            ProfilerHook(config.profiler),
            SplunkForwarderHook(config.splunkForwarder),
            JiraLogs(),
            LateUbuntuSysstat()
        ).forEach { it.run(ssh, jira, flow) }
    }
}
