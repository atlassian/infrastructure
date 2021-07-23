package com.atlassian.performance.tools.infrastructure.api.network

import com.atlassian.performance.tools.ssh.api.Ssh

interface SshServerRoom : AutoCloseable {
    fun serveSsh(name: String): Ssh
}