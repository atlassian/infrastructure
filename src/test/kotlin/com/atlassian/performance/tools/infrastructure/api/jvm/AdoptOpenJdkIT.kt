package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage
import org.junit.Test

class AdoptOpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        SshUbuntuImage.runSoloSsh { ssh ->
            JstatSupport(AdoptOpenJDK()).shouldSupportJstat(ssh)
        }
    }
}