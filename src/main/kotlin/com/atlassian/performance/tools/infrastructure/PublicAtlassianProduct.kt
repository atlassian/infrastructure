package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

internal class PublicAtlassianProduct(
    private val archiveName: String,
    private val destination: String
) {
    internal fun install(sshConnection: SshConnection) {
        download(sshConnection)
        unpack(sshConnection)
    }

    private fun download(sshConnection: SshConnection) {
        val jiraArchiveUri = URI("https://product-downloads.atlassian.com/software/jira/downloads/$archiveName")
        sshConnection.execute("wget -P $destination -q $jiraArchiveUri", Duration.ofMinutes(5))
    }

    private fun unpack(sshConnection: SshConnection) {
        sshConnection.execute("tar -xzf $destination/$archiveName --directory $destination", Duration.ofMinutes(1))
    }
}