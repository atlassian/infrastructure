package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.Level
import java.time.Duration

/**
 * @param tarGzPath remote path to a tar.gz distribution
 */
class TarGzDistribution(
    private val tarGzPath: String
) : ProductDistribution {

    override fun install(ssh: SshConnection, destination: String): String {
        ssh.execute(
            "tar --extract --gzip --file $tarGzPath --directory $destination",
            timeout = Duration.ofMinutes(1),
            stdout = Level.TRACE,
            stderr = Level.TRACE
        )
        val unpackedDirectory = ssh
            .execute(
                "tar --list --file $tarGzPath | head --lines=1",
                timeout = Duration.ofMinutes(1)
            )
            .output
            .split("/")
            .first()
        return "$destination/$unpackedDirectory"
    }
}
