package com.atlassian.performance.tools.infrastructure.api.jira.report

import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Points to a remote SSH **file**. For directories use a [FileListing] instead.
 *
 * @param [remotePath] Points to a file on a remote system.
 *                     Relative to the SSH shell default directory (predominantly the user home directory).
 */
class StaticReport(
    private val remotePath: String
) : Report {

    override fun locate(ssh: SshConnection): List<String> = listOf(remotePath)
}
