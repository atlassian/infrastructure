package com.atlassian.performance.tools.infrastructure.api.storage

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Downloads Jira Software installers from the official Jira downloads site.
 * @since 4.6.0
 */
@Deprecated("Use `PublicJiraSoftwareDistribution` from `com.atlassian.performance.tools.infrastructure.api.distribution` package.")
class PublicJiraSoftwareDistributions {
    /**
     *  @since 4.6.0
     */
    @Suppress("DEPRECATION")
    fun get(version: String): ProductDistribution {
        return object : ProductDistribution {
            override fun install(ssh: SshConnection, destination: String): String {
                return PublicJiraSoftwareDistribution(version).install(ssh, destination)
            }
        }
    }
}