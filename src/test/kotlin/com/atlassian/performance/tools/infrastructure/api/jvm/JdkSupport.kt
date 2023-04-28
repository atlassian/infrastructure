package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat

class JdkSupport(
    private val jdk: VersionedJavaDevelopmentKit
) {
    fun shouldHaveJavaHomeSet(expectedJavaPath: String) {
        SshUbuntuContainer.Builder().build().start().use { ubuntu ->
            val ssh = ubuntu.toSsh()
            ssh.newConnection().use { connection ->
                jdk.install(connection)
                val javaHomeOutput = connection.execute("source ~/.profile; echo \$JAVA_HOME").output

                assertThat(javaHomeOutput).contains(expectedJavaPath)
            }
        }
    }
}
