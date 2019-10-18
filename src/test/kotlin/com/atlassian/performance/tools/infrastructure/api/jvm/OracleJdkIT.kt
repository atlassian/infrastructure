package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.junit.Test

class OracleJdkIT {

    @Test
    fun shouldSupportJstatAndThreadDumps() {
        SshUbuntuContainer().start().use { ssh ->
            ssh.toSsh().newConnection().use { connection ->
                val jdk = OracleJDK()
                JstatSupport(jdk).shouldSupportJstat(connection)
                ThreadDumpTest().shouldGatherThreadDump(jdk, connection)
            }
        }
    }
}