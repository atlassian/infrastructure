package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.time.Duration

class OracleJDK : VersionedJavaDevelopmentKit {
    private val logger: Logger = LogManager.getLogger(this::class.java)
    private val jdkUpdate = 131
    private val jdkArchive = "jdk-8u$jdkUpdate-linux-x64.tar.gz"
    private val jdkUrl = URI.create("https://download.oracle.com/otn-pub/java/jdk/8u$jdkUpdate-b11/d54c1d3a095b4ff2b6607d096fa80163/$jdkArchive")
    private val path = "~/jdk1.8.0_$jdkUpdate"
    private val jreBin = "$path/jre/bin/"
    private val bin = "$path/bin/"
    override val jstatMonitoring = Jstat(bin)

    @Deprecated(
        message = "Use JavaDevelopmentKit.jstatMonitoring instead.",
        replaceWith = ReplaceWith("jstatMonitoring")
    )
    val jstat = jstatMonitoring

    override fun getMajorVersion() = 8

    override fun install(connection: SshConnection) {
        download(connection)
        connection.execute("tar -xzf $jdkArchive")
        connection.execute("echo '${use()}' >> ~/.profile")
        JdkFonts().install(connection)
    }

    override fun use(): String = "export PATH=$jreBin:$bin:${'$'}PATH; export JAVA_HOME=$path"

    override fun command(options: String) = "${jreBin}java $options"

    private fun download(connection: SshConnection) {
        val attempts = 0..3
        for (attempt in attempts) {
            try {
                connection.execute(
                    cmd = "curl --silent --location --remote-name --cookie 'oraclelicense=a' $jdkUrl",
                    timeout = Duration.ofSeconds(120)
                )
                break
            } catch (e: Exception) {
                logger.debug("Attempt #$attempt to download Oracle JDK failed")
                if (attempt == attempts.last) {
                    throw Exception("Failed to download Oracle JDK despite $attempt attempts", e)
                }
            }
        }
    }
}
