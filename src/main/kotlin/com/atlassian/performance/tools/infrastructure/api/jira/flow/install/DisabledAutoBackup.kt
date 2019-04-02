package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.EmptyReport
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PassingStart
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.ssh.api.SshConnection

class DisabledAutoBackup : Install {

    override fun install(ssh: SshConnection, jira: InstalledJira): Start {
        ssh.execute("echo jira.autoexport=false > ${jira.home}/jira-config.properties")
        return PassingStart(EmptyReport())
    }
}
