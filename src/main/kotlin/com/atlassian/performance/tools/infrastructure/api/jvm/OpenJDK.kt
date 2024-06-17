package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.PreinstalledJDK
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.jvm.UbuntuJavaHome
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

class OpenJDK : VersionedJavaDevelopmentKit {
    override val jstatMonitoring: Jstat = Jstat("")

    override fun getMajorVersion() = 8

    override fun install(connection: SshConnection) {
        Ubuntu().install(
            connection,
            listOf("openjdk-8-jdk"),
            Duration.ofMinutes(10)
        )
        UbuntuJavaHome().install(connection)
    }

    override fun use(): String {
        return UbuntuJavaHome().use()
    }

    override fun command(options: String): String {
        return "java $options"
    }

    fun toPreinstalledJdk(): JavaDevelopmentKit = PreinstalledJDK(javaBin = "java", jstatMonitoring = jstatMonitoring)
}
