package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI
import java.time.Duration

class AdoptOpenJDK11 : VersionedJavaDevelopmentKit {
    private val jdkVersion = "-11.0.1+13"
    private val jdkArchive = "OpenJDK11U-jdk_x64_linux_hotspot_11.0.1_13.tar.gz"

    /**
     * Download URL
     * https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.1+13/OpenJDK11U-jdk_x64_linux_hotspot_11.0.1_13.tar.gz
     * or
     * https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.1+13/OpenJDK11U-jdk_x64_linux_openj9_jdk-11.0.1_13_openj9-0.11.0_11.0.1_13.tar.gz
     */
    private val jdkUrl = URI("https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk$jdkVersion/$jdkArchive")
    private val path = "~/jdk$jdkVersion"
    private val jreBin = "$path/jre/bin/"
    private val jdkBin = "$path/bin/"
    override val jstatMonitoring: Jstat = Jstat(jdkBin)

    override fun getMajorVersion() = 11

    override fun install(connection: SshConnection) {
        download(connection)
        connection.execute("tar -xzf $jdkArchive")
        connection.execute("echo '${use()}' >> ~/.profile")
        JdkFonts().install(connection)
    }

    override fun use(): String =
        "export PATH=${'$'}PATH:$jreBin:$jdkBin && export JAVA_HOME=$path"

    override fun command(options: String) = "${jdkBin}java $options"

    private fun download(connection: SshConnection) {
        Ubuntu().install(connection, listOf("curl"))
        IdempotentAction(
            description = "Download AdoptOpenJDK",
            action = {
                connection.execute(
                    cmd = "curl -s -L -O -k $jdkUrl",
                    timeout = Duration.ofMinutes(3)
                )
            }
        ).retry(
            maxAttempts = 3,
            backoff = ExponentialBackoff(Duration.ofSeconds(5))
        )
    }
}
