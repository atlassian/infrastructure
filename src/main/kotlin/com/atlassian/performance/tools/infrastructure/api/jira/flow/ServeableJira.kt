package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.ssh.api.SshConnection

class ServeableJira(
    private val started: StartedJira,
    private val serveHook: Serve
) {
    fun serve(
        ssh: SshConnection
    ): ReportableJira {
        val reportHook = serveHook.serve(ssh, started)
        return ReportableJira(reportHook)
    }
}
