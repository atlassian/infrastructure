package com.atlassian.performance.tools.infrastructure.jvm

import com.atlassian.performance.tools.infrastructure.PreinstalledJDK
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class UbuntuJavaHome : VersionedJavaDevelopmentKit {
    override val jstatMonitoring: Jstat = Jstat("")

    override fun getMajorVersion() = 8

    override fun install(connection: SshConnection) {
        Ubuntu().install(
            connection,
            listOf("openjdk-8-jdk"),
            Duration.ofMinutes(10)
        )
        // parse `java-1.8.0-openjdk-arm64       1081       /usr/lib/jvm/java-1.8.0-openjdk-arm64`
        val javaHome = connection.safeExecute("update-java-alternatives --list").output.split(" ").last().trim()
        connection.execute("echo 'export JAVA_HOME=$javaHome' >> ~/.profile")
    }

    override fun use(): String {
        return "source ~/.profile"
    }

    override fun command(options: String): String {
        return "java $options"
    }

    fun toPreinstalledJdk(): JavaDevelopmentKit = PreinstalledJDK(javaBin = "java", jstatMonitoring = jstatMonitoring)
}
