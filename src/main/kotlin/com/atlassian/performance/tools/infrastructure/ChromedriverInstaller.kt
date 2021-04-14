package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.net.URI
import java.time.Duration

internal class ChromedriverInstaller(private val uri: URI) {
    internal fun install(ssh: SshConnection) {
        ParallelExecutor().execute(
            {
                HttpResource(uri).download(ssh, "chromedriver.zip")
                Ubuntu().install(ssh, listOf("unzip"))
                ssh.execute("unzip -n -q chromedriver.zip")
                ssh.execute("chmod +x chromedriver")
                ssh.execute("sudo ln -s `pwd`/chromedriver /usr/bin/chromedriver")
            },
            {
                Ubuntu().install(ssh, listOf("zip", "libglib2.0-0", "libnss3"), Duration.ofMinutes(2))
            }
        )
    }

    companion object {
        fun getLatestVersion(minorVersion: String?): String {
            val httpclient = HttpClients.createDefault()
            val versionSuffix = if (minorVersion != null) "_$minorVersion" else "";
            val get = HttpGet("https://chromedriver.storage.googleapis.com/LATEST_RELEASE$versionSuffix")
            val response = httpclient.execute(get)
            return response.entity.content.bufferedReader().use { it.readLine() }
        }
    }
}
