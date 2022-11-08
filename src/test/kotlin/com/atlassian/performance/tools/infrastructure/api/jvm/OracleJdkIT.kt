package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test

class OracleJdkIT {

    @Test
    fun shouldSupportJstatAndThreadDumps() {
        val jdk = OracleJDK()
        JstatSupport(jdk).shouldSupportJstat()
        SshUbuntuContainer.Builder().build().start().use { ubuntu ->
            val ssh = ubuntu.toSsh()
            ssh.newConnection().use { connection ->
                ThreadDumpTest().shouldGatherThreadDump(jdk, connection)
            }
        }
    }
}