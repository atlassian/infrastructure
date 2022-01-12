package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection

class CrowdEncryptedPasswordProvider(
    private val jiraDatabaseSchemaName: String,
    private val passwordPlainText: String,
    private val passwordEncryptedWithAtlassianSecurityPasswordEncoder: String,
    private val sqlClient: SshSqlClient
) : JiraUserEncryptedPasswordProvider {

    override fun getEncryptedPassword(ssh: SshConnection): String {
        val sqlResult =
            sqlClient.runSql(ssh, "select attribute_value from ${jiraDatabaseSchemaName}.cwd_directory_attribute where attribute_name = 'user_encryption_method';").output
        return when {
            sqlResult.contains("plaintext") -> passwordPlainText
            sqlResult.contains("atlassian-security") -> passwordEncryptedWithAtlassianSecurityPasswordEncoder
            else -> throw RuntimeException("Unknown jira user password encryption type")
        }
    }
}