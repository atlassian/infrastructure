package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.ssh.api.Ssh

interface Infrastructure : AutoCloseable { // TODO rename to ServerRoom

    val subnet: String

    /**
     * @return can be reached by the caller via [TcpNode.publicIp] and by the rest of the infra via [TcpNode.privateIp]
     */
    fun serveTcp(name: String): TcpNode
    fun serveHttp(name: String): HttpNode
    fun serve(name: String, tcpPorts: List<Int>, udpPorts: List<Int>): TcpNode
    fun serveSsh(name: String): Ssh
}