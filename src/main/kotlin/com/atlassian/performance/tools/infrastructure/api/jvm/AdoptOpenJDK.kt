package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

class AdoptOpenJDK : VersionedJavaDevelopmentKit {
    private val jdkVersion = "8u172-b11"
    private val jdkArchive = "OpenJDK8_x64_Linux_jdk$jdkVersion.tar.gz"
    private val jdkUrl = URI("https://github.com/AdoptOpenJDK/openjdk8-releases/releases/download/jdk$jdkVersion/$jdkArchive")
    private val path = "~/jdk$jdkVersion"
    private val jreBin = "$path/jre/bin/"
    private val jdkBin = "$path/bin/"
    override val jstatMonitoring: Jstat = Jstat(jdkBin)

    override fun getMajorVersion() = 8

    override fun install(connection: SshConnection) {
        download(connection)
        connection.execute("tar -xzf $jdkArchive")
        connection.execute("echo '${use()}' >> ~/.profile")
    }

    override fun use(): String {
        return "export PATH=${'$'}PATH:$jreBin:$jdkBin; export JAVA_HOME=$path"
    }

    override fun command(options: String) = "${jdkBin}java $options"

    private fun download(connection: SshConnection) {
        Ubuntu().install(connection, listOf("curl"))
        IdempotentAction(
            description = "Download AdoptOpenJDK",
            action = {
                connection.execute(
                    cmd = "curl -s -L -O -k $jdkUrl",
                    timeout = Duration.ofSeconds(50)
                )
            }
        ).retry(
            maxAttempts = 3,
            backoff = ExponentialBackoff(Duration.ofSeconds(5))
        )
    }
}
