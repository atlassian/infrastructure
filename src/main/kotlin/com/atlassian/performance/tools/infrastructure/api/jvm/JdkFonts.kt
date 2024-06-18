package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class JdkFonts {

    /**
     * @see <a href="https://confluence.atlassian.com/jirakb/jira-server-7-13-or-later-fails-with-fontconfiguration-error-when-installing-on-linux-operating-systems-964956221.html">Jira fonts installation</a>
     * @see <a href="https://jira.atlassian.com/browse/CONFSRVDEV-8954">Captcha failing to load because of missing fonts</a>
     */
    fun install(ssh: SshConnection) {
        Ubuntu().install(ssh, listOf("fontconfig"))
    }
}
