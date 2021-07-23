package com.atlassian.performance.tools.infrastructure.api.network

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode

interface HttpServerRoom {

    fun serveHttp(name: String): HttpNode
}