package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.ssh.api.Ssh

/**
 * Has open TCP sockets.
 */
class TcpHost(
    val ip: String,
    val port: Int,
    val name: String,
    val ssh: Ssh
)
