package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.ssh.api.SshConnection

interface JiraUserEncryptedPasswordProvider {
    fun getEncryptedPassword(ssh: SshConnection): String
}
