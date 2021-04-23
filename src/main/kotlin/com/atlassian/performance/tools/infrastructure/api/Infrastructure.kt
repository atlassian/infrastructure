package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost

interface Infrastructure : AutoCloseable {
    
    /**
     * @return can be reached by the caller via [TcpHost.publicIp] and by the rest of the infra via [TcpHost.privateIp]
     */
    fun serve(port: Int, name: String): TcpHost
}