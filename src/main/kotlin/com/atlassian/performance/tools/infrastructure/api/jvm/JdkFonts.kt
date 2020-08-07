package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.ssh.api.SshConnection

class JdkFonts {

    fun install(ssh: SshConnection) {
        val packages = listOf(
            "fonts-dejavu-core", // https://confluence.atlassian.com/bitbucketserverkb/captcha-image-doesn-t-render-779171210.html
            "fontconfig" // https://jira.atlassian.com/browse/CONFSRVDEV-8954
        )
        Ubuntu().install(ssh, packages)
    }
}
