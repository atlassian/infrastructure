package com.atlassian.performance.tools.infrastructure.docker

import com.atlassian.performance.tools.jvmtasks.api.Backoff
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

internal class DeadContainerCheck(
    private val container: String,
    private val ssh: SshConnection,
    private val base: Backoff
) : Backoff {
    override fun backOff(attempt: Int): Duration {
        val status = ssh.execute("sudo docker inspect  --format '{{.State.Status}}' $container").output.trim()
        if (status == "exited") {
            val logs = ssh.execute("sudo docker logs $container").errorOutput.trim()
            throw Exception("$container exited, logs: $logs")
        }
        return base.backOff(attempt)
    }
}