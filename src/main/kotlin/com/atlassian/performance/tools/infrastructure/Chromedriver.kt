package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

internal class Chromedriver(private val version: String) {
    internal fun install(ssh: SshConnection) {
        ssh.execute("wget -q \"https://chromedriver.storage.googleapis.com/$version/chromedriver_linux64.zip\"")
        Ubuntu().install(ssh, listOf("zip", "libglib2.0-0", "libnss3"), Duration.ofMinutes(2))
        ssh.execute("unzip -n -q chromedriver_linux64.zip")
        ssh.execute("chmod +x chromedriver")
        ssh.execute("sudo ln -s `pwd`/chromedriver /usr/bin/chromedriver")
    }
}