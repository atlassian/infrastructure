package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage.Companion.runSoloSsh
import org.junit.Test

class AdoptOpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        runSoloSsh { ssh ->
            JstatSupport(AdoptOpenJDK11()).shouldSupportJstat(ssh)
        }
    }
}