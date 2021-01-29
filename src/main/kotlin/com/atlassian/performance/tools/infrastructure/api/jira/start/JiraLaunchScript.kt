package com.atlassian.performance.tools.infrastructure.api.jira.start

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class JiraLaunchScript : JiraStart {

    override fun start(
        ssh: SshConnection,
        installed: InstalledJira
    ): StartedJira {
        ssh.execute(
            "${installed.jdk.use()}; ${installed.installation}/bin/start-jira.sh",
            Duration.ofMinutes(1)
        )
        val pid = ssh
            .execute("cat ${installed.installation}/work/catalina.pid")
            .output
            .trim()
            .toInt()
        return StartedJira(installed, pid)
    }
}
