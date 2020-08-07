package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage
import org.junit.Test

class OracleJdkIT {

    @Test
    fun shouldSupportJstatAndThreadDumps() {
        SshUbuntuImage.runSoloSsh { ssh ->
            val jdk = OracleJDK()
            JstatSupport(jdk).shouldSupportJstat(ssh)
            ThreadDumpTest().shouldGatherThreadDump(jdk, ssh)
        }
    }
}