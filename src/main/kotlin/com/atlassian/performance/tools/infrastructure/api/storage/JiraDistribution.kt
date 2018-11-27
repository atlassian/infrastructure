package com.atlassian.performance.tools.infrastructure.api.storage

import com.atlassian.performance.tools.ssh.api.SshConnection

interface JiraDistribution {
    /**
     * Installs a Jira distribution on a remote system via SSH.
     *
     * @param [ssh] Lets Jira be installed remotely.
     * @param [destination] Remote directory, which should contain the installed Jira.
     *                      Should already exist.
     *                      Should be relative to the current [ssh] working directory.
     * @return Remote directory containing a canonical Jira installation.
     *         For example: `atlassian-jira-software-7.13.0-standalone`.
     *         Should be a subdirectory of [destination]. Should be relative to the current [ssh] working directory.
     *
     * @since 4.6.0
     */
    fun install(
        ssh: SshConnection,
        destination: String
    ): String
}