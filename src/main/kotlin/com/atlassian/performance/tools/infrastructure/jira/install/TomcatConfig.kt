package com.atlassian.performance.tools.infrastructure.jira.install

import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class TomcatConfig(
    private val jira: InstalledJira,
    private val connectorPort: Int
) {
    fun fixHttp(
        ssh: SshConnection
    ): TomcatConfig {
        val httpPort = jira.http.tcp.port
        if (connectorPort == httpPort) {
            return this;
        }
        Sed().replace(
            ssh,
            "<Connector port=\"$connectorPort\"",
            "<Connector port=\"$httpPort\"",
            jira.installation.resolve("conf/server.xml").path
        )
        return TomcatConfig(jira, httpPort)
    }
}
