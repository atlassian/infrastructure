package com.atlassian.performance.tools.infrastructure.virtualusers

import com.atlassian.performance.tools.ssh.SshConnection

interface ResultsTransport {

    fun transportResults(
        targetDirectory: String,
        sshConnection: SshConnection
    )
}