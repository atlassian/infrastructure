package com.atlassian.performance.tools.infrastructure.api.jira.hook.start

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.hook.JiraNodeHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.server.StartedJira
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class JiraLaunchScript : JiraStart {

    override fun start(
        ssh: SshConnection,
        installed: InstalledJira,
        hooks: JiraNodeHooks
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
