package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.api.SshConnection

internal fun SshConnection.SshResult.assertInterruptedJava() {
    if (exitStatus !in listOf(0, 130)) {
        throw Exception("$this doesn't look like an interrupted Java process")
    }
}