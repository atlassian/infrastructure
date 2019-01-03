package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration
import java.time.Duration.ofMinutes


/**
 * We have no control over the chrome version. We install the latest stable chrome version. It may cause not repeatable builds.
 */
class Chrome : Browser {
    override fun install(ssh: SshConnection) {
        ssh.execute("wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | sudo apt-key add")
        ssh.execute("echo 'deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main' | sudo tee -a /etc/apt/sources.list.d/google-chrome.list")
        Ubuntu().install(ssh, listOf("google-chrome-stable"), ofMinutes(3))
        installLatestChromedriver(ssh)
    }

    private fun installLatestChromedriver(ssh : SshConnection) {
        ssh.execute("wget -q \"https://chromedriver.storage.googleapis.com/`wget -q -O - https://chromedriver.storage.googleapis.com/LATEST_RELEASE`/chromedriver_linux64.zip\"")
        Ubuntu().install(ssh, listOf("zip"), Duration.ofMinutes(2))
        ssh.execute("unzip -n -q chromedriver_linux64.zip")
        ssh.execute("chmod +x chromedriver")
        ssh.execute("sudo ln -s `pwd`/chromedriver /usr/bin/chromedriver")
    }
}