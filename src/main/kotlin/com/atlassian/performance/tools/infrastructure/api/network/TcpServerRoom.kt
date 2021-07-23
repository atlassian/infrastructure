package com.atlassian.performance.tools.infrastructure.api.network

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode

interface TcpServerRoom : AutoCloseable {

    /**
     * @return reachable by the caller via [TcpNode.publicIp] and by the rest of the network via [TcpNode.privateIp]
     */
    fun serveTcp(name: String): TcpNode
    fun serveTcp(name: String, tcpPorts: List<Int>, udpPorts: List<Int>): TcpNode
}