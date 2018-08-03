package com.atlassian.performance.tools.infrastructure.jvm

import com.atlassian.performance.tools.infrastructure.os.Ubuntu
import com.atlassian.performance.tools.ssh.SshConnection
import java.time.Duration

class OpenJDK : JavaDevelopmentKit {
    override fun install(connection: SshConnection) {
        Ubuntu().install(
            connection,
            listOf("openjdk-8-jdk"),
            Duration.ofMinutes(5)
        )
    }

    override fun use(): String {
        return ""
    }

    override fun command(options: String): String {
        return "java $options"
    }

    fun toPreinstalledJdk(): PreinstalledJDK = PreinstalledJDK(javaBin = "java")
}