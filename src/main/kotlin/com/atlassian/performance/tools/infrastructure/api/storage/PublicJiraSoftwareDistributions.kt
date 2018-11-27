package com.atlassian.performance.tools.infrastructure.api.storage

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

/**
 * Downloads Jira installers from the official Jira downloads site.
 */
class PublicJiraSoftwareDistributions {
    fun get(version: String): JiraDistribution {
        return object : JiraDistribution {
            override fun install(ssh: SshConnection, destination: String): String {
                download(ssh, destination)
                unpack(ssh, destination)
                return "$destination/atlassian-jira-software-$version-standalone"
            }

            private fun download(ssh: SshConnection, destination: String) {
                val jiraArchiveUri = URI("https://product-downloads.atlassian.com/software/jira/downloads/${getJiraArchiveName()}")
                ssh.execute("wget -P $destination -q $jiraArchiveUri", Duration.ofMinutes(5))
            }

            private fun unpack(ssh: SshConnection, destination: String) {
                ssh.execute("tar -xzf $destination/${getJiraArchiveName()} --directory $destination", Duration.ofMinutes(1))
            }

            private fun getJiraArchiveName() = "atlassian-jira-software-$version.tar.gz"
        }
    }
}