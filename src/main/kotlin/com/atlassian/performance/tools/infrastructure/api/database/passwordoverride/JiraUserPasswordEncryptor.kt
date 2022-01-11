package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.util.function.Function

enum class JiraUserPasswordEncryptionType {
    PLAIN_TEXT, ENCRYPTED
}

interface JiraUserPasswordEncryptor {
    fun getEncryptedPassword(plainTextPassword: String): String
}

interface JiraUserPasswordEncryptorProvider {
    fun get(jiraUserPasswordEncryptionType: JiraUserPasswordEncryptionType): JiraUserPasswordEncryptor
}


interface JiraUserPasswordEncryptionTypeService {
    fun getEncryptionType(ssh: SshConnection, sqlClient: SshSqlClient): JiraUserPasswordEncryptionType
}

class DefaultJiraUserPasswordEncryptionTypeService(
    private val jiraDatabaseSchemaName: String
) : JiraUserPasswordEncryptionTypeService {

    override fun getEncryptionType(ssh: SshConnection, sqlClient: SshSqlClient): JiraUserPasswordEncryptionType {
        val sqlResult =
            sqlClient.runSql(ssh, "select attribute_value from ${jiraDatabaseSchemaName}.cwd_directory_attribute where attribute_name = 'user_encryption_method';").output
        return when {
            sqlResult.contains("plaintext") -> JiraUserPasswordEncryptionType.PLAIN_TEXT
            sqlResult.contains("atlassian-security") -> JiraUserPasswordEncryptionType.ENCRYPTED
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


class DefaultJiraUserPasswordEncryptorProvider(passwordEncryptFunction: Function<String, String>) : JiraUserPasswordEncryptorProvider {
    private val encryptors = mapOf(
        JiraUserPasswordEncryptionType.PLAIN_TEXT to PlainTextJiraUserPasswordEncryptor(),
        JiraUserPasswordEncryptionType.ENCRYPTED to EncryptedJiraUserPasswordEncryptor(passwordEncryptFunction)
    )

    override fun get(jiraUserPasswordEncryptionType: JiraUserPasswordEncryptionType) = encryptors[jiraUserPasswordEncryptionType]!!

}