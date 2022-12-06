package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.PublicAtlassianProduct
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Downloads Jira Software installers from the official Jira downloads site.
 * @since 4.8.0
 */
class PublicJiraSoftwareDistribution(private val version: String) : ProductDistribution {
    override fun install(ssh: SshConnection, destination: String): String {
        return PublicAtlassianProduct(
            archiveName = "atlassian-jira-software-$version.tar.gz",
            destination = destination
        ).install(ssh)
    }
}