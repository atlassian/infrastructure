package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.assertj.core.api.Assertions.assertThat

class JdkSupport(
    private val jdk: VersionedJavaDevelopmentKit
) {
    fun shouldHaveJavaHomeSet(expectedJavaPath: String) {
        DockerInfrastructure().use { infra ->
            val ssh = infra.serveSsh()
            ssh.newConnection().use { connection ->
                jdk.install(connection)
                val javaHomeOutput = connection.execute("source ~/.profile; echo \$JAVA_HOME").output

                assertThat(javaHomeOutput).contains(expectedJavaPath)
            }
        }
    }
}
