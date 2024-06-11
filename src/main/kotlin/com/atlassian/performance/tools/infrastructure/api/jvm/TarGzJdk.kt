package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration.ofSeconds

class TarGzJdk private constructor(
    private val majorVersion: String,
    private val minorVersion: String,
    private val patchVersion: String,
    private val downloadUrl: String
) : VersionedJavaDevelopmentKit {
    private val version = "$majorVersion.$minorVersion.$patchVersion"
    private val javaHome = "~/jdk-$version"

    override fun getMajorVersion() = majorVersion.toInt()

    override fun install(connection: SshConnection) {
        val archive = "jdk-$version.tar.gz"
        IdempotentAction("download JDK $version from $downloadUrl") {
            connection.execute(
                cmd = "curl --location $downloadUrl > $archive",
                timeout = ofSeconds(120)
            )
        }.retry(3, ExponentialBackoff(ofSeconds(10)))
        connection.execute("mkdir $javaHome")
        // unzip to javaHome regardless of the main directory name in the archive
        val tempDir = connection.execute("mktemp -d").output.lines().first()
        connection.execute("tar --extract --gunzip --file $archive --directory $tempDir")
        val extractedDir = connection.execute("ls $tempDir").output.lines().first()
        connection.execute("mv $tempDir/$extractedDir/* $javaHome")
        connection.execute("rm -rf $tempDir")
        connection.execute("echo '${use()}' >> ~/.profile")
    }

    override fun use(): String = "export PATH=$javaHome/bin:\$PATH; export JAVA_HOME=$javaHome"

    override fun command(options: String) = "$javaHome/bin/java $options"

    override val jstatMonitoring = Jstat("$javaHome/bin/")

    class Builder(
        private var majorVersion: String,
        private var minorVersion: String,
        private var patchVersion: String,
        private var downloadUrl: String
    ) {

        fun version(
            majorVersion: String,
            minorVersion: String,
            patchVersion: String,
            downloadUrl: String
        ) = apply {
            this.majorVersion = majorVersion
            this.minorVersion = minorVersion
            this.patchVersion = patchVersion
            this.downloadUrl = downloadUrl
        }

        fun build(): TarGzJdk = TarGzJdk(
            majorVersion = majorVersion,
            minorVersion = minorVersion,
            patchVersion = patchVersion,
            downloadUrl = downloadUrl
        )

    }
}
