package com.atlassian.performance.tools.infrastructure.jira.install

import com.atlassian.performance.tools.ssh.api.Ssh

/**
 * Has open TCP sockets.
 */
class TcpServer(
    val ip: String,
    val publicPort: Int,
    val privatePort: Int,
    val name: String,
    val ssh: Ssh
) {
    constructor(
        ip: String,
        port: Int,
        name: String,
        ssh: Ssh
    ) : this(
        ip,
        port,
        port,
        name,
        ssh
    )
}
