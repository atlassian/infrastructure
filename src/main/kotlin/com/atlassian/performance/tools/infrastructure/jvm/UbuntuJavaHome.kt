package com.atlassian.performance.tools.infrastructure.jvm

import com.atlassian.performance.tools.infrastructure.PreinstalledJDK
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

internal class UbuntuJavaHome {
    fun install(connection: SshConnection) {
        // parse `java-1.8.0-openjdk-arm64       1081       /usr/lib/jvm/java-1.8.0-openjdk-arm64`
        val javaHome = connection.safeExecute("update-java-alternatives --list").output.split(" ").last().trim()
        connection.execute("echo 'export JAVA_HOME=$javaHome' >> ~/.profile")
    }

    fun use(): String {
        return "source ~/.profile"
    }
}
