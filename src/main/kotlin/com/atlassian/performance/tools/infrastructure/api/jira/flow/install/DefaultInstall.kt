package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.JiraLogs
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.JstatUpgrade
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.RestUpgrade
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultInstall(
    private val config: JiraNodeConfig
) : Install {
    override fun install(
        ssh: SshConnection,
        jira: InstalledJira
    ): Start = InstallSequence(listOf(
        JiraHomeProperty(),
        SystemLog(),
        PassingInstall(JiraLogs()),
        JvmConfig(config),
        DisabledAutoBackup(),
        UbuntuSysstat(),
        SplunkForwarderInstall(config.splunkForwarder),
        ProfilerInstall(config.profiler),
        PassingInstall(JiraLogs()),
        PassingInstall(JstatUpgrade()),
        PassingInstall(RestUpgrade(config.launchTimeouts, "admin", "admin"))
    )).install(ssh, jira)
}
