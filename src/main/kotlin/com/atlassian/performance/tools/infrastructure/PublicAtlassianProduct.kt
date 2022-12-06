package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

internal class PublicAtlassianProduct(
    private val archiveName: String,
    private val destination: String
) {
    internal fun install(sshConnection: SshConnection): String {
        download(sshConnection)
        unpack(sshConnection)
        val directoryName = discoverDirectoryName(sshConnection)
        return "$destination/$directoryName"
    }

    private fun download(sshConnection: SshConnection) {
        val jiraArchiveUri = URI("https://product-downloads.atlassian.com/software/jira/downloads/$archiveName")
        sshConnection.execute("mkdir --parents $destination")
        HttpResource(jiraArchiveUri).download(sshConnection, "$destination/$archiveName", Duration.ofMinutes(6))
    }

    private fun unpack(sshConnection: SshConnection) {
        sshConnection.execute(
            "tar --extract --gzip --file $destination/$archiveName --directory $destination",
            timeout = Duration.ofMinutes(1)
        )
    }

    private fun discoverDirectoryName(sshConnection: SshConnection): String = sshConnection
        .execute(
            "tar --list --file $destination/$archiveName | head --lines=1",
            timeout = Duration.ofMinutes(1)
        )
        .output
        .split("/")
        .first()
}
