package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration.ofSeconds


/**
 * Supports Oracle JDK 11 and later.
 */
class VersionedOracleJdk private constructor(
    private val majorVersion: String,
    private val minorVersion: String,
    private val patchVersion: String
) : VersionedJavaDevelopmentKit {
    private val version = "$majorVersion.$minorVersion.$patchVersion"
    private val downloadUrl =
        "https://download.oracle.com/java/$majorVersion/archive/jdk-${version}_linux-x64_bin.tar.gz"
    private val javaHome = "~/jdk-$version"

    override fun getMajorVersion() = majorVersion.toInt()

    override fun install(connection: SshConnection) {
        IdempotentAction("download JDK $version") {
            connection.execute(
                cmd = "curl $downloadUrl > jdk-$version.tar.gz",
                timeout = ofSeconds(120)
            )
        }.retry(3, ExponentialBackoff(ofSeconds(10)))
        connection.execute("tar --extract --gunzip --file jdk-$version.tar.gz")
        connection.execute("echo '${use()}' >> ~/.profile")
        JdkFonts().install(connection)
    }

    override fun use(): String = "export PATH=$javaHome/bin:\$PATH; export JAVA_HOME=$javaHome"

    override fun command(options: String) = "$javaHome/bin/java $options"

    override val jstatMonitoring = Jstat("$javaHome/bin/")

    class Builder {
        private var majorVersion: String = "17"
        private var minorVersion: String = "0"
        private var patchVersion: String = "11"

        fun version(majorVersion: String, minorVersion: String, patchVersion: String) = apply {
            this.majorVersion = majorVersion
            this.minorVersion = minorVersion
            this.patchVersion = patchVersion
        }

        fun build(): VersionedOracleJdk = VersionedOracleJdk(
            majorVersion = majorVersion,
            minorVersion = minorVersion,
            patchVersion = patchVersion
        )

    }

}
