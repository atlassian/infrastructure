package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test

class OpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        SshUbuntuContainer.Builder().build().start().use { ssh ->
            ssh.toSsh().newConnection().use { connection ->
                JstatSupport(OpenJDK()).shouldSupportJstat(connection)
            }
        }
    }
}