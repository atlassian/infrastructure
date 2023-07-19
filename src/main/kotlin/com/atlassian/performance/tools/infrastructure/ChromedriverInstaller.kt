package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

internal class ChromedriverInstaller(private val uri: URI) {
    internal fun install(ssh: SshConnection) {
        ParallelExecutor().execute(
            {
                HttpResource(uri).download(ssh, "chromedriver.zip")
                Ubuntu().install(ssh, listOf("unzip"))
                ssh.execute("unzip -n -q chromedriver.zip -d extraction-dir")
                val executable = ssh.execute("find `pwd`/extraction-dir -name chromedriver -type f").output.lines().first()
                if (executable.isBlank()) {
                    throw Exception("No executable found in extracted ChromeDriver archive")
                }
                ssh.execute("chmod +x $executable")
                ssh.execute("sudo ln -s $executable /usr/bin/chromedriver")
            },
            {
                Ubuntu().install(ssh, listOf("zip", "libglib2.0-0", "libnss3"), Duration.ofMinutes(2))
            }
        )
    }
}
