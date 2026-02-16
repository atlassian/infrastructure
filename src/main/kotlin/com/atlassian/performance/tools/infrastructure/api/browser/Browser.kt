package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Installs browser and WebDriver. See [Chrome] or [com.atlassian.performance.tools.infrastructure.api.browser.chromium.Chromium69].
 * Browser must provide public no-args constructor.
 */
interface Browser {
    /**
     * Installs browser and WebDriver.
     *
     * @param ssh ssh connection to ubuntu based machine.
     */
    fun install(ssh: SshConnection)
}