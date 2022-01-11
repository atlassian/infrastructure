package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.function.Function

interface JiraUserPasswordEncryptor {
    fun getEncryptedPassword(plainTextPassword: String): String
}

interface JiraUserPasswordEncryptorProvider {
    fun getEncryptor(ssh: SshConnection, sqlClient: SshSqlClient): JiraUserPasswordEncryptor
}

class DefaultJiraUserPasswordEncryptorProvider(
    private val jiraDatabaseSchemaName: String,
    private val plainTextPasswordEncryptor: JiraUserPasswordEncryptor,
    private val encryptedPasswordEncryptor: JiraUserPasswordEncryptor
) : JiraUserPasswordEncryptorProvider {

    override fun getEncryptor(ssh: SshConnection, sqlClient: SshSqlClient): JiraUserPasswordEncryptor {
        val sqlResult =
            sqlClient.runSql(ssh, "select attribute_value from ${jiraDatabaseSchemaName}.cwd_directory_attribute where attribute_name = 'user_encryption_method';").output
        return when {
            sqlResult.contains("plaintext") -> plainTextPasswordEncryptor
            sqlResult.contains("atlassian-security") -> encryptedPasswordEncryptor
            else -> throw RuntimeException("Unknown jira user password encryption type")
        }
    }
}

class EncryptedJiraUserPasswordEncryptor(
    private val passwordEncryptFunction: Function<String, String>
) : JiraUserPasswordEncryptor {
    override fun getEncryptedPassword(plainTextPassword: String) = passwordEncryptFunction.apply(plainTextPassword)
}

class PlainTextJiraUserPasswordEncryptor : JiraUserPasswordEncryptor {
    override fun getEncryptedPassword(plainTextPassword: String) = plainTextPassword
}
