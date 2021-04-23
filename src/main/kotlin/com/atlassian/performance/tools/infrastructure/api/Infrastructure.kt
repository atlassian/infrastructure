package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.ssh.api.Ssh

interface Infrastructure : AutoCloseable { // TODO rename to ServerRoom

    val subnet: String

    /**
     * @return can be reached by the caller via [TcpHost.publicIp] and by the rest of the infra via [TcpHost.privateIp]
     */
    fun serveTcp(name: String): TcpHost
    fun serveSsh(name: String): Ssh
}