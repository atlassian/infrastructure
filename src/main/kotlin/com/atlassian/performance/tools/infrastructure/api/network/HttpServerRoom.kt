package com.atlassian.performance.tools.infrastructure.api.network

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode

interface HttpServerRoom : AutoCloseable {

    fun serveHttp(name: String): HttpNode
}