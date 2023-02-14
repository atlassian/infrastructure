package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

class HttpResource(
    private val uri: URI
) {

    fun download(
        ssh: SshConnection,
        destination: String
    ) {
        download(
            ssh,
            destination,
            Duration.ofMinutes(4)
        )
    }

    fun download(
        ssh: SshConnection,
        destination: String,
        timeout: Duration
    ) {
        Ubuntu().install(ssh, listOf("lftp"), Duration.ofMinutes(2))
        ssh.execute(
            """curl -s "$uri" -o $destination""",
            timeout
        )
    }
}
