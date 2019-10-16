package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.StaticBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

/**
 * Harvested from https://stash.atlassian.com/projects/JIRASERVER/repos/jira-performance-tests/pull-requests/630
 */
class S3HostedJdk : VersionedJavaDevelopmentKit {
    private val jdkVersion = "1.8.0"
    private val jdkUpdate = 131
    private val jdkArchive = "jdk${jdkVersion}_$jdkUpdate-linux-x64.tar.gz"
    private val jdkUrl = URI.create("https://s3.amazonaws.com/packages_java/$jdkArchive")
    private val jdkBin = "~/jdk${jdkVersion}_$jdkUpdate/jre/bin/"
    private val bin = "~/jdk${jdkVersion}_$jdkUpdate/bin/"
    override val jstatMonitoring = Jstat(bin)

    override fun getMajorVersion() = 8

    override fun install(connection: SshConnection) {
        download(connection)
        connection.execute("tar -xzf $jdkArchive")
        connection.execute("echo '${use()}' >> ~/.bashrc")
    }

    private fun download(connection: SshConnection) {
        IdempotentAction("download JDK") {
            connection.execute(
                cmd = "curl -s -L -O -k $jdkUrl",
                timeout = Duration.ofMinutes(4)
            )
        }.retry(
            maxAttempts = 3,
            backoff = StaticBackoff(Duration.ofSeconds(4))
        )
    }

    override fun use(): String = "export PATH=$jdkBin:$bin:${'$'}PATH"

    override fun command(options: String) = "${jdkBin}java $options"
}
