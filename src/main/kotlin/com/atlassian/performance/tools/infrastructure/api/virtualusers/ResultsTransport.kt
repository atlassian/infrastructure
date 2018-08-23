package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.ssh.SshConnection

interface ResultsTransport {

    fun transportResults(
        targetDirectory: String,
        sshConnection: SshConnection
    )
}