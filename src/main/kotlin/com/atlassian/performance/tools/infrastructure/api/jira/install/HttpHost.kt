package com.atlassian.performance.tools.infrastructure.api.jira.install

import java.net.URI

class HttpHost internal constructor(
    val tcp: TcpHost,
    private val basePath: String,
    private val supportsTls: Boolean
) {

    fun addressPublicly(): URI = address(tcp.publicIp)
    fun addressPrivately(): URI = address(tcp.privateIp)
    fun addressPrivately(userName: String, password: String): URI = address(tcp.privateIp, "$userName:$password@")

    private fun address(ip: String, userInfo: String = ""): URI {
        val scheme = if (supportsTls) "https" else "http"
        return URI("$scheme://$userInfo$ip:${tcp.port}$basePath/")
    }
}
