package com.atlassian.performance.tools.infrastructure.api.jira.start

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.ssh.api.Ssh
import java.time.Duration

class JiraLaunchScript : JiraStart {

    override fun start(
        installed: InstalledJira
    ): StartedJira {
        val installation = installed.installation
        Ssh(installation.host).newConnection().use { ssh ->
            ssh.execute(
                "${installed.jdk.use()}; ${installation.path}/bin/start-jira.sh",
                Duration.ofMinutes(1)
            )
            val pid = ssh
                .execute("cat ${installation.path}/work/catalina.pid")
                .output
                .trim()
                .toInt()
            return StartedJira(installed, pid)
        }
    }
}
