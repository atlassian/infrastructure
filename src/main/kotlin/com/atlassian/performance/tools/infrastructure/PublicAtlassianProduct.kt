package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.distribution.TarGzDistribution
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

internal class PublicAtlassianProduct(
    private val archiveName: String
) : ProductDistribution {

    override fun install(ssh: SshConnection, destination: String): String {
        val jiraArchiveUri = URI("https://product-downloads.atlassian.com/software/jira/downloads/$archiveName")
        ssh.execute("mkdir --parents $destination")
        val archive = "$destination/$archiveName"
        HttpResource(jiraArchiveUri).download(ssh, archive, Duration.ofMinutes(6))
        return TarGzDistribution(archive).install(ssh, destination)
    }
}
