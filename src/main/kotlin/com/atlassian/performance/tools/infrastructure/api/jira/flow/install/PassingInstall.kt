package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PassingStart
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.PassingUpgrade
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.ssh.api.SshConnection

class PassingInstall(
    private val start: Start
) : Install {

    constructor(
        upgrade: Upgrade
    ) : this(
        PassingStart(upgrade)
    )

    constructor(
        report: Report
    ) : this(
        PassingUpgrade(report)
    )

    override fun install(ssh: SshConnection, jira: InstalledJira): Start = start
}
