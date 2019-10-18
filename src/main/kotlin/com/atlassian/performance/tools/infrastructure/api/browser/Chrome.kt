package com.atlassian.performance.tools.infrastructure.api.browser

import com.atlassian.performance.tools.infrastructure.ChromedriverInstaller
import com.atlassian.performance.tools.infrastructure.ParallelExecutor
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.net.URI
import java.time.Duration.ofMinutes


/**
 * We have no control over the chrome version. We install the latest stable chrome version. It may cause not repeatable builds.
 */
class Chrome : Browser {
    override fun install(ssh: SshConnection) {
        ssh.execute("wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | sudo apt-key add")
        ssh.execute("echo 'deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main' | sudo tee -a /etc/apt/sources.list.d/google-chrome.list")
        ParallelExecutor().execute(
            { Ubuntu().install(ssh, listOf("google-chrome-stable"), ofMinutes(5)) },
            {
                val version = getLatestVersion()
                ChromedriverInstaller(URI("https://chromedriver.storage.googleapis.com/$version/chromedriver_linux64.zip")).install(ssh)
            }
        )
    }

    private fun getLatestVersion(): String {
        val httpclient = HttpClients.createDefault()
        val get = HttpGet("https://chromedriver.storage.googleapis.com/LATEST_RELEASE")
        val response = httpclient.execute(get)
        return response.entity.content.bufferedReader().use { it.readLine() }
    }
}