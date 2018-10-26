package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration.ofMinutes


/**
 * We have no control over the chrome version. We install the latest stable chrome version. It may cause not repeatable builds.
 */
class Chrome : Browser {
    override fun install(ssh: SshConnection) {
        ssh.execute("wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | sudo apt-key add")
        ssh.execute("echo 'deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main' | sudo tee -a /etc/apt/sources.list.d/google-chrome.list")
        val ubuntu = Ubuntu()
        ubuntu.install(ssh, listOf("google-chrome-stable"), ofMinutes(2))
    }
}