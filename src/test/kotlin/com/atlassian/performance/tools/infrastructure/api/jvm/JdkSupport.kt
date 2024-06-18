package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import java.io.File

class JdkSupport(
    private val jdk: VersionedJavaDevelopmentKit
) {
    fun shouldHaveJavaHomeSet(expectedJavaPath: String) {
        SshUbuntuContainer.Builder().build().start().use { ubuntu ->
            val ssh = ubuntu.toSsh()
            ssh.newConnection().use { connection ->
                jdk.install(connection)
                val javaHomeOutput = connection.execute("${jdk.use()}; echo \$JAVA_HOME").output

                assertThat(javaHomeOutput).contains(expectedJavaPath)
            }
        }
    }

    fun shouldLoadFont() {
        SshUbuntuContainer.Builder().build().start().use { ubuntu ->
            val ssh = ubuntu.toSsh()
            ssh.newConnection().use { connection ->
                jdk.install(connection)
                connection.upload(
                    File(javaClass.getResource("FontTest.java").toURI()),
                    "FontTest.java"
                )
                connection.execute("${jdk.use()}; javac FontTest.java")
                connection.execute("${jdk.use()}; java FontTest")
            }
        }
    }
}
