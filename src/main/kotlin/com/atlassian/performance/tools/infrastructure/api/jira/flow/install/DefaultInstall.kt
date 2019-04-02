package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.JiraLogs
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.JstatUpgrade
import com.atlassian.performance.tools.ssh.api.SshConnection

class DefaultInstall(
    private val config: JiraNodeConfig
) : Install {
    override fun install(
        ssh: SshConnection,
        jira: InstalledJira
    ): Start = InstallSequence(listOf(
        JiraHomeProperty(),
//        SystemLog(),
        JvmConfig(config),
        DisabledAutoBackup(),
        UbuntuSysstat(),
        SplunkForwarderInstall(config.splunkForwarder),
        ProfilerInstall(config.profiler),
        PassingInstall(JiraLogs()),
        PassingInstall(JstatUpgrade())
//        PassingInstall(RestUpgrade(config.launchTimeouts, "admin", "admin"))  TODO("This always fails if the DB is not set up")
    )).install(ssh, jira)
}
