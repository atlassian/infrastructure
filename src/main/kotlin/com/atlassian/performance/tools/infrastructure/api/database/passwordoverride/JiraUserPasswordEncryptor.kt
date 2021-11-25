package com.atlassian.performance.tools.infrastructure.api.database.passwordoverride

import com.atlassian.performance.tools.infrastructure.database.SshSqlClient
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

interface JiraUserPasswordEncryptor {
    fun getEncryptedPassword(ssh: SshConnection): String
}

internal class DefaultJiraUserPasswordEncryptor(
    private val passwordEncryptFunction: (String) -> String,
    private val userPasswordPlainText: String,
    private val sqlClient: SshSqlClient,
    private val jiraDatabaseSchemaName: String
): JiraUserPasswordEncryptor {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun getEncryptedPassword(ssh: SshConnection): String {
        return if (shouldUseEncryption(ssh)) {
            logger.debug("Using credential with encrypted password")
            passwordEncryptFunction(userPasswordPlainText)
        } else {
            logger.debug("Using credential with plain text password")
            userPasswordPlainText
        }
    }

    private fun shouldUseEncryption(ssh: SshConnection): Boolean {
        val sqlResult =
            sqlClient.runSql(ssh, "select attribute_value from ${jiraDatabaseSchemaName}.cwd_directory_attribute where attribute_name = 'user_encryption_method';").output
        return when {
            sqlResult.contains("plaintext") -> false
            sqlResult.contains("atlassian-security") -> true
            else -> {
                logger.warn("Unknown user_encryption_method. Assuming encrypted password should be used")
                true
            }
        }
    }

}