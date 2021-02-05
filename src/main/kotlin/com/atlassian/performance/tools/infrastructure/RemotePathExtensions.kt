package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.distribution.ProductDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath
import com.atlassian.performance.tools.ssh.api.SshConnection

fun JiraHomeSource.downloadRemotely(
    ssh: SshConnection
) = RemotePath(ssh.getHost(), this.download(ssh))

fun ProductDistribution.installRemotely(
    ssh: SshConnection,
    destination: String
) = RemotePath(ssh.getHost(), this.install(ssh, destination))