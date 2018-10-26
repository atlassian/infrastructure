package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.ssh.api.SshConnection

interface Browser {
    fun install(ssh: SshConnection)
}