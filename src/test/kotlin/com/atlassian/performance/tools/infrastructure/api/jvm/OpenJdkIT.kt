package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage
import org.junit.Test

class OpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        SshUbuntuImage.runSoloSsh { ssh ->
            JstatSupport(OpenJDK()).shouldSupportJstat(ssh)
        }
    }
}