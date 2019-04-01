package com.atlassian.performance.tools.infrastructure.api.jira.flow.start

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.PassingUpgrade
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.ssh.api.SshConnection

class PassingStart(
    private val upgrade: Upgrade
) : Start {

    constructor(
        report: Report
    ) : this(
        PassingUpgrade(report)
    )

    override fun start(ssh: SshConnection, jira: InstalledJira): Upgrade = upgrade
}