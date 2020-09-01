package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage
import org.junit.Test

class OpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        SshUbuntuImage.runSoloSsh { ssh ->
            JstatSupport(OpenJDK11()).shouldSupportJstat(ssh)
        }
    }
}