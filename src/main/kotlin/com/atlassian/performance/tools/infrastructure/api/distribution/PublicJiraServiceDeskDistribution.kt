package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.PublicAtlassianProduct
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Downloads Jira Service Desk installers from the official Jira downloads site.
 * @since 4.8.0
 */
class PublicJiraServiceDeskDistribution(private val version: String) : ProductDistribution {
    override fun install(ssh: SshConnection, destination: String): String {
        return PublicAtlassianProduct(
            archiveName = "atlassian-servicedesk-$version.tar.gz",
            destination = destination
        ).install(ssh)
    }
}