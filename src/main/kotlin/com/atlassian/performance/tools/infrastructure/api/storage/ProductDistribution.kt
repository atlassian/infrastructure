package com.atlassian.performance.tools.infrastructure.api.storage

import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 *
 * [ProductDistribution] can be used to install an Atlassian product (for example Jira, Confluence, Bitbucket)
 * on a remote system.
 *
 * @since 4.6.0
 */
interface ProductDistribution {
    /**
     * Installs a product distribution on a remote system via SSH.
     *
     * @param [ssh] Lets product be installed remotely.
     * @param [destination] Remote directory, which should contain the installed product.
     *                      Should already exist.
     *                      Should be relative to the current [ssh] working directory.
     * @return Remote directory containing a canonical product installation.
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