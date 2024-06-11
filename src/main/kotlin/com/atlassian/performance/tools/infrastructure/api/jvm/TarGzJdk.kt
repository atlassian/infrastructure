package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration.ofSeconds

class TarGzJdk private constructor(
    private val version: String,
    private val downloadUrl: String
) : VersionedJavaDevelopmentKit {
    private var javaHome: String? = null

    override fun getMajorVersion() = version.substringBefore(".").toInt()

    override fun install(connection: SshConnection) {
        val archive = "jdk-$version.tar.gz"
        IdempotentAction("download JDK $version from $downloadUrl") {
            connection.execute(
                cmd = "curl --location $downloadUrl > $archive",
                timeout = ofSeconds(120)
            )
        }.retry(3, ExponentialBackoff(ofSeconds(10)))
        connection.execute("mkdir $javaHome")
        // unzip to an empty temp dir to get main dir name
        val tempDir = connection.execute("mktemp -d").output.lines().first()
        connection.execute("tar --extract --gunzip --file $archive --directory $tempDir")
        val extractedJdkDir = connection.execute("ls $tempDir").output.lines().first()
        connection.execute("mv $tempDir/$extractedJdkDir ~")
        connection.execute("rm -rf $tempDir")
        javaHome = "~/$extractedJdkDir"
        connection.execute("echo '${use()}' >> ~/.profile")
    }

    override fun use(): String {
        check(javaHome != null) { "JDK needs to be installed first" }
        return "export PATH=$javaHome/bin:\$PATH; export JAVA_HOME=$javaHome"
    }

    override fun command(options: String): String {
        check(javaHome != null) { "JDK needs to be installed first" }
        return "$javaHome/bin/java $options"
    }

    override val jstatMonitoring: Jstat by lazy {
        check(javaHome != null) { "JDK needs to be installed first" }
        Jstat("$javaHome/bin/")
    }

    class Builder {
        private var version: String = "17.0.11_9"
        private var downloadUrl: String =
            "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_x64_linux_hotspot_17.0.11_9.tar.gz"

        fun version(version: String, downloadUrl: String) = apply {
            this.version = version
            this.downloadUrl = downloadUrl
        }

        fun build(): VersionedJavaDevelopmentKit = TarGzJdk(
            version = version,
            downloadUrl = downloadUrl
        )

    }
}
