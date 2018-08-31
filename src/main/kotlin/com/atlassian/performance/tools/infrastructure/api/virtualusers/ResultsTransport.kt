package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.ssh.api.SshConnection

interface ResultsTransport {

    fun transportResults(
        targetDirectory: String,
        sshConnection: SshConnection
    )
}