package com.atlassian.performance.tools.infrastructure.api.jira.flow.report

import com.atlassian.performance.tools.ssh.api.SshConnection

class StaticReport(
    private val remoteLocations: List<String>
) : Report {

    constructor(
        remoteLocation: String
    ) : this(
        listOf(remoteLocation)
    )

    override fun locate(ssh: SshConnection): List<String> = remoteLocations
}
